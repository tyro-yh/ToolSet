package com.batchtask.manage.api;

import com.batchtask.manage.entity.BatchTask;
import com.batchtask.manage.util.BatchTaskManager;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 优速任务初始化
 * @author yh
 */
@Component
@EnableScheduling
public class YsPolicyJob {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BatchTaskManager batchTaskManager;

    @Autowired
    public YsPolicyJob(BatchTaskManager batchTaskManager) {
        this.batchTaskManager = batchTaskManager;
    }

    private final ExecutorService executorService = new ThreadPoolExecutor(1, 50, 3000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024),
            new BasicThreadFactory.Builder().namingPattern("YsPolicyJob-schedule-pool-%d").daemon(true).build(),
            new ThreadPoolExecutor.AbortPolicy());

    @Scheduled(fixedRate = 5000)
    public void ysPolicyJob() {
        executorService.submit(() -> {
            logger.info("线程1定时任务模拟多任务开始");
            BatchTask task = new BatchTask();
            task.setTaskKey(getTaskKey("YsSftpDownload"));
            task.setTaskType("YsSftpDownload");
            task.setPartnerCode("YS");
            task.setStartTime(new Date());
            task.setNextExecTime(new Date());
            logger.info("线程1尝试添加任务："+task.getTaskKey());
            batchTaskManager.addTask(task);
            task.setDependentTaskKeys(task.getTaskKey());
            task.setTaskKey(getTaskKey("YsProcessOrder"));
            task.setTaskType("YsProcessOrder");
            logger.info("线程1尝试添加任务："+task.getTaskKey());
            batchTaskManager.addTask(task);
        });
    }

    @Scheduled(fixedRate = 5000)
    public void ysPolicyJob2() {
        executorService.submit(() -> {
            logger.info("线程2定时任务模拟多任务开始");
            BatchTask task = new BatchTask();
            task.setTaskKey(getTaskKey("YsSftpDownload"));
            task.setTaskType("YsSftpDownload");
            task.setPartnerCode("YS");
            task.setStartTime(new Date());
            task.setNextExecTime(new Date());
            logger.info("线程2尝试添加任务："+task.getTaskKey());
            batchTaskManager.addTask(task);
            task.setDependentTaskKeys(task.getTaskKey());
            task.setTaskKey(getTaskKey("YsProcessOrder"));
            task.setTaskType("YsProcessOrder");
            logger.info("线程2尝试添加任务："+task.getTaskKey());
            batchTaskManager.addTask(task);
        });
    }

    private String getTaskKey(String taskType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return taskType +
                sdf.format(new Date());
    }
}
