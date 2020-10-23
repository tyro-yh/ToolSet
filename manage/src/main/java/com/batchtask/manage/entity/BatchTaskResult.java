package com.batchtask.manage.entity;

import lombok.Data;

import java.util.Date;

/**
 * 任务执行结果
 * @author
 */
@Data
public class BatchTaskResult {
    /**
     * 执行状态
     */
    private String status;

    /**
     * 执行结果
     */
    private String resultData;

    /**
     * 下次执行时间
     */
    private Date nextExecTime;
}
