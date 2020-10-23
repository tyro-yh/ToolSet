package com.batchtask.manage.service.impl;

import com.batchtask.manage.constants.StatusEnum;
import com.batchtask.manage.entity.BatchTask;
import com.batchtask.manage.entity.BatchTaskResult;
import com.batchtask.manage.service.IBatchTaskHandler;
import com.batchtask.manage.util.BatchTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单入库业务实现处理器 仅演示用
 * todo 按需实现
 * @author yh
 */
@Service
public class YsProcessOrderBatchTaskHandler implements IBatchTaskHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BatchTaskManager batchTaskManager;

    @Autowired
    public YsProcessOrderBatchTaskHandler(BatchTaskManager batchTaskManager) {
        this.batchTaskManager = batchTaskManager;
        batchTaskManager.registerHandler("YsProcessOrder",this);
    }

    @Override
    public BatchTaskResult handle(BatchTask task) {
        logger.info("订单入库开始");
        BatchTaskResult result = new BatchTaskResult();
        result.setStatus(StatusEnum.COMPLETE.getValue());
        result.setResultData("订单入库成功！");
        logger.info("订单入库结束");
        return result;
    }
}
