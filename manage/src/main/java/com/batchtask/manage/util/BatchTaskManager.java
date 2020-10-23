package com.batchtask.manage.util;

import com.batchtask.manage.constants.StatusEnum;
import com.batchtask.manage.dao.BatchTaskDao;
import com.batchtask.manage.entity.BatchTask;
import com.batchtask.manage.entity.BatchTaskResult;
import com.batchtask.manage.service.IBatchTaskHandler;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务管理器
 * @author yh
 */
@Component
@EnableScheduling
public class BatchTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(BatchTaskManager.class);

    /**
     * 任务重试次数
     */
    @Value("${task.retry.times}")
    private int taskRetryTimes;

    /**
     * 任务重试时间间隔(小时)
     */
    @Value("${task.retry.interval}")
    private int tryIntervalTime;

    /**
     * Redis锁超时时间(秒)
     */
    @Value("${redis.lock.interval}")
    private int redisLockInterval;

    /**
     * 本机服务器ip
     */
    private final String applicationServerIp;

    @Resource
    private BatchTaskDao batchTaskDao;

    private final RedisUtil redisUtil;

    /**
     * 任务执行类映射集
     */
    private final Map<String, IBatchTaskHandler> registeredHandlers = new HashMap<>();

    private final ExecutorService executorService = new ThreadPoolExecutor(5, 50, 3000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024),
            new BasicThreadFactory.Builder().namingPattern("BatchTaskManager-schedule-pool-%d").daemon(true).build(),
            new ThreadPoolExecutor.AbortPolicy());

    @Autowired
    public BatchTaskManager(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
        String serverIp = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            if (address != null) {
                serverIp = address.getHostAddress();
            }
        } catch (Exception e) {
            logger.error("BatchTaskManager 获取服务器ip异常");
        }
        this.applicationServerIp = serverIp;
    }

    /**
     * 新增任务
     * @param task 任务数据模型
     */
    public void addTask(BatchTask task) {
        //锁时间
        long value = System.currentTimeMillis()+redisLockInterval*1000;

        //获取加锁状态
        boolean lockFlag = redisUtil.lock(task.getTaskKey(),String.valueOf(value));

        //如果获取锁则进一步判断唯一键
        if (lockFlag) {
            Long l = batchTaskDao.queryTaskByTaskKey(task.getTaskKey());
            if (l == 0) {
                logger.info("新增任务开始");
                try {
                    batchTaskDao.addTask(task);
                } catch (Exception e) {
                    logger.error("新增任务失败，任务号："+task.getTaskKey()+",{}",CommonUtil.getStackTrace(e));
                }
                logger.info("新增任务结束，任务号："+task.getTaskKey());
            } else {
                logger.info("新增任务取消，已存在的任务号："+task.getTaskKey());
            }
            //解锁
            redisUtil.unlock(task.getTaskKey(),String.valueOf(value));
        }
    }

    /**
     * 加锁设置任务状态为执行中
     * @param task 任务数据模型
     * @return 是否锁定成功
     */
    public boolean lockTask(BatchTask task) {
        //锁时间
        long value = System.currentTimeMillis()+redisLockInterval*1000;

        //获取加锁状态
        boolean lockFlag = redisUtil.lock(task.getTaskKey(),String.valueOf(value));
        //锁定任务标识
        boolean flag = false;
        //如果获取锁则进一步修改任务状态为执行中
        if (lockFlag) {
            try {
                String status = batchTaskDao.queryStatusByTaskKey(task.getTaskKey());
                if (!StatusEnum.IN_EXECUTION.getValue().equals(status)) {
                    //增长执行次数
                    task.setExecuteTimes(task.getExecuteTimes()+1);
                    //设置执行机器ip
                    task.setExecutorIp(applicationServerIp);
                    //设置状态为执行中
                    task.setStatus(StatusEnum.IN_EXECUTION.getValue());
                    batchTaskDao.updateTask(task);
                    status = batchTaskDao.queryStatusByTaskKey(task.getTaskKey());
                    if (StatusEnum.IN_EXECUTION.getValue().equals(status)) {
                        flag = true;
                    }
                }
            } catch (Exception e) {
                logger.error("锁定任务失败，任务号："+task.getTaskKey()+",{}",CommonUtil.getStackTrace(e));
            }
            //解锁
            redisUtil.unlock(task.getTaskKey(),String.valueOf(value));
        }
        return flag;
    }

    /**
     * 完成任务更新状态。适用于没有执行类，由人工操作的任务，如理赔批次确认
     * @param task 任务数据模型
     */
    public void finishTask(BatchTask task) {
        logger.info("完成任务更新状态开始");
        try {
            batchTaskDao.updateTask(task);
        } catch (Exception e) {
            logger.error("完成任务更新状态失败，任务号："+task.getTaskKey()+",{}",CommonUtil.getStackTrace(e));
        }
        logger.info("完成任务更新状态结束，任务号："+task.getTaskKey());
    }

    /**
     * 注册任务执行接口实现类
     * @param taskType 任务类型
     * @param handler 任务执行类
     */
    public void registerHandler(String taskType, IBatchTaskHandler handler){
        registeredHandlers.put(taskType,handler);
        logger.info("任务管理器注册执行服务：taskType-"+taskType+" handler-"+handler.getClass().getName());
    }

    /**
     * 定时调用此方法，进行任务执行
     */
//    @Scheduled(cron = "0 0 0/3 * * ?")
    @SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
    @Scheduled(fixedRate = 10000)
    public void executeTasks(){
        logger.info("定时执行任务开始");
        Date newDate = new Date();
        List<BatchTask> batchTasks = batchTaskDao.getAllPendingTasks();
        for (BatchTask batchTask : batchTasks) {
            //如果设置了下次执行时间则判断下次执行时间是否在当前时间之前
            //modify 2020-10-20 nextExecTime 可空 调整判断逻辑，startTime/endTime不在作为筛选条件
            if (batchTask.getNextExecTime() != null && batchTask.getNextExecTime().after(newDate)) {
                continue;
            }
            //如果执行次数已经超过设定的最大重试次数
            if (batchTask.getExecuteTimes() > taskRetryTimes) {
                continue;
            }
            //判断所有依赖任务是否执行完毕
            if (!StringUtils.isEmpty(batchTask.getDependentTaskKeys())) {
                String[] dTaskKeys = batchTask.getDependentTaskKeys().split(",");
                Long l = batchTaskDao.queryPendingTasksByTaskKeys(dTaskKeys);
                if (l > 0) {
                    continue;
                }
            }
            //获取任务执行实现类
            IBatchTaskHandler handler = registeredHandlers.get(batchTask.getTaskType());
            if (handler != null) {
                //加锁设置任务状态为执行中，成功则调用执行类
                if (lockTask(batchTask)) {
                    try {
                        Thread.sleep(3000L);
                        executeTask(handler,batchTask);
                    } catch (InterruptedException e) {
                        logger.error("任务管理器休眠期发生异常");
                        batchTask.setStatus(StatusEnum.INIT.getValue());
                        batchTask.setResultData("管理器休眠异常回滚状态为初始化！");
                        batchTaskDao.updateTask(batchTask);
                    }
                }
            } else {
                logger.error("定时执行任务号:"+batchTask.getTaskKey()+" 失败已跳过\n");
                batchTask.setStatus(StatusEnum.FAIL_END.getValue());
                batchTask.setResultData("没有找到相关处理实现类！");
                batchTaskDao.updateTask(batchTask);
            }
        }
        logger.info("定时执行任务结束");
    }

    public void executeTask(IBatchTaskHandler handler,BatchTask batchTask) {
        executorService.submit(() -> {
            try {
                logger.info("定时执行任务号："+batchTask.getTaskKey()+" 调用实现类："+handler.getClass().getName());
                batchTask.setStartTime(new Date());
                BatchTaskResult result = handler.handle(batchTask);
                if (result != null && !StringUtils.isEmpty(result.getStatus())) {
                    StatusEnum se = StatusEnum.getEnumByValue(result.getStatus());
                    if (se == null) {
                        se = StatusEnum.FAIL_END;
                    }
                    logger.info("定时执行任务号："+batchTask.getTaskKey()+" 执行状态："+se.getName());
                    batchTask.setStatus(result.getStatus());
                    batchTask.setResultData(result.getResultData());
                    //如果状态不为成功则设置下次执行时间
                    if (se != StatusEnum.COMPLETE) {
                        //modify 2020-10-20 下次执行时间可以通过handle返回
                        if (result.getNextExecTime() != null) {
                            batchTask.setNextExecTime(result.getNextExecTime());
                        } else {
                            //modify 2020-10-20 未设置下次执行时间的则使用固定间隔
                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            c.add(Calendar.HOUR,tryIntervalTime);
                            batchTask.setNextExecTime(c.getTime());
                        }
                    }
                } else {
                    logger.error("定时归档任务号:"+batchTask.getTaskKey()+" 处理结果为空！\n");
                    batchTask.setStatus(StatusEnum.FAIL_END.getValue());
                    batchTask.setResultData("处理结果为空！");
                }
            } catch (Exception e) {
                logger.error("定时归档任务号:"+batchTask.getTaskKey()+" 失败,{}",CommonUtil.getStackTrace(e));
                batchTask.setStatus(StatusEnum.FAIL_END.getValue());
                batchTask.setResultData("执行任务发生异常！");
            }
            batchTask.setEndTime(new Date());
            batchTaskDao.updateTask(batchTask);
        });
    }

    /**
     * 定时调用此方法，进行任务归档
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void archiveTasks(){
        logger.info("定时归档任务开始");
        List<BatchTask> batchTasks = batchTaskDao.getAllCompleteTasks();
        int i = 0;
        for (BatchTask batchTask : batchTasks) {
            try {
                archiveTask(batchTask);
                i++;
            } catch (Exception e) {
                logger.error("定时归档任务号:"+batchTask.getTaskKey()+" 失败已跳过,{}",CommonUtil.getStackTrace(e));
            }
        }
        logger.info("定时归档任务结束共计归档："+i);
    }

    @Transactional(rollbackFor = {Exception.class,RuntimeException.class})
    public void archiveTask(BatchTask batchTask) {
        try {
            batchTaskDao.archiveTasks(batchTask.getTaskKey());
            batchTaskDao.deleteTasks(batchTask.getTaskKey());
        } catch (Exception e) {
            logger.error("定时归档任务号:"+batchTask.getTaskKey()+" 失败,{}",CommonUtil.getStackTrace(e));
            throw new RuntimeException("定时归档任务号:"+batchTask.getTaskKey()+" 失败");
        }
    }
}
