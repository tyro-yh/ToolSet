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
     * @param task
     * @return
     */
    public BatchTaskResult handle(BatchTask task);
}
