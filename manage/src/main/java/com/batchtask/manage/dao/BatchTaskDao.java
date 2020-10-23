package com.batchtask.manage.dao;

import com.batchtask.manage.entity.BatchTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * mybatis数据库操作接口
 * @author
 */
@Mapper
public interface BatchTaskDao {

    /**
     * 新增任务
     * @param task 任务数据模型
     */
    void addTask(BatchTask task);

    /**
     * 更新任务状态
     * @param task 任务数据模型
     */
    void updateTask(BatchTask task);

    /**
     * 归档任务
     * @param taskKey 任务key
     */
    void archiveTasks(@Param("taskKey") String taskKey);

    /**
     * 删除任务
     * @param taskKey 任务key
     */
    void deleteTasks(@Param("taskKey") String taskKey);

    /**
     * 获取所有待执行的任务
     * @return 所有待执行的任务
     */
    @Select("select * from batch_task where status in ('1','4')")
    List<BatchTask> getAllPendingTasks();

    /**
     * 获取已完成或失败取消的任务
     * @return 已完成或失败取消的任务
     */
    @Select("select * from batch_task where status in ('3','5')")
    List<BatchTask> getAllCompleteTasks();

    /**
     * 任务唯一性校验
     * @param taskKey 任务key
     * @return 对应的key的记录数
     */
    Long queryTaskByTaskKey(@Param("taskKey") String taskKey);

    /**
     * 根据taskKeys列表判断是否存在待处理任务
     * @param taskKeys 任务key列表
     * @return 待处理任务数
     */
    Long queryPendingTasksByTaskKeys(@Param("taskKeys") String[] taskKeys);

    /**
     * 根据taskKey获取对应任务的状态
     * @param taskKey 任务key
     * @return 对应任务状态
     */
    String queryStatusByTaskKey(@Param("taskKey") String taskKey);

    /**
     * 锁定任务并设置为执行中
     * @param taskKey 任务key
     */
    void lockTask(@Param("taskKey") String taskKey);
}
