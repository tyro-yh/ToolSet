package com.batchtask.manage.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 任务数据模型
 * 同构 BatchTask
 * @author yh
 */
@Data
public class BatchTaskArchive {
    private Long id;

    private String taskKey;

    private String taskType;

    private String taskData;

    private String status;

    private Date createTime;

    private Date startTime;

    private Date endTime;

    private String resultData;

    private String executorIp;

    private int executeTimes;

    private Date nextExecTime;

    private String dependentTaskKeys;

    private String partnerCode;
}
