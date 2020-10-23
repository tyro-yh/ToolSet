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
 * sftp下载业务实现处理器 仅演示用
 * todo 按需实现
 * @author yh
 */
@Service
public class YsSftpDownloadBatchTaskHandler implements IBatchTaskHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BatchTaskManager batchTaskManager;

    @Autowired
    public YsSftpDownloadBatchTaskHandler(BatchTaskManager batchTaskManager) {
        this.batchTaskManager = batchTaskManager;
        batchTaskManager.registerHandler("YsSftpDownload",this);
    }

    @Override
    public BatchTaskResult handle(BatchTask task) {
        logger.info("sftp下载开始");
        BatchTaskResult result = new BatchTaskResult();
        result.setStatus(StatusEnum.COMPLETE.getValue());
        result.setResultData("sftp下载成功！");
        logger.info("sftp下载结束");
        return result;
    }
}
