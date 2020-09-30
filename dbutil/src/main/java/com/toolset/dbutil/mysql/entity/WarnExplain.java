package com.toolset.dbutil.mysql.entity;

/**
 * sql执行风险枚举类
 * @author yh
 */
public enum WarnExplain {
    //全表扫描风险
    ALL("ALL","T0-全表扫描 必须优化\n"),
    //全索引树扫描风险
    index("index","T0-全索引树扫描 必须优化\n"),
    //连表未使用索引风险
    join("Using join buffer","T1-连表操作没有相关索引 建议优化\n");

    /**
     * 风险code
     */
    private String code;
    /**
     * 风险描述
     */
    private String message;

    private WarnExplain(String code,String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
