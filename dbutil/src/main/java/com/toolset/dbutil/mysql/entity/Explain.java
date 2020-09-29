package com.toolset.dbutil.mysql.entity;

import lombok.Data;

/**
 * 执行计划表
 * @author yh
 */
@Data
public class Explain {

    /** 执行计划的id mysql 按由大到小执行， 相同的则从上到下执行*/
    private String id;
    /** 查询的类型*/
    private String select_type;
    /** 查询涉及的表可能是别名*/
    private String table;
    /** 查询的访问类型*/
    private String type;
    /** 查询使用的索引键*/
    private String possible_keys;
    /** mysql解析优化后决定使用的键*/
    private String key;
    /** 估算的结果集行数*/
    private String rows;
    /** mysql解决查询的详细情况*/
    private String Extra;
}
