package com.batchtask.manage.service;

import com.batchtask.manage.entity.BatchTask;
import com.batchtask.manage.entity.BatchTaskResult;

/**
 * 任务执行接口
 * @author yh
 */
public interface IBatchTaskHandler {
    /**
     * 实现任务执行具体逻辑
     * @param task 任务数据模型
     * @return 任务执行结果
     */
    BatchTaskResult handle(BatchTask task);
}
