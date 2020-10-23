package com.batchtask.manage.entity;

import lombok.Data;

import java.util.Date;

/**
 * 任务数据模型
 * @author yh
 */
@Data
public class BatchTask {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 任务key
     */
    private String taskKey;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务请求串
     */
    private String taskData;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 执行开始时间
     */
    private Date startTime;

    /**
     * 执行结束时间
     */
    private Date endTime;

    /**
     * 执行结果串
     */
    private String resultData;

    /**
     * 执行服务器ip
     */
    private String executorIp;

    /**
     * 执行次数
     */
    private int executeTimes;

    /**
     * 下次执行时间
     */
    private Date nextExecTime;

    /**
     * 依赖任务keys 逗号分割
     */
    private String dependentTaskKeys;

    /**
     * 渠道方代码
     */
    private String partnerCode;
}
