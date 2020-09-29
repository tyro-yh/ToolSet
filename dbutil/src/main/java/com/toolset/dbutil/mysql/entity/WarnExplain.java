package com.toolset.dbutil.mysql.entity;

public enum WarnExplain {
    ALL("ALL","T0-全表扫描 必须优化\n"),
    index("index","T0-全索引树扫描 必须优化\n"),
    join("Using join buffer","T1-连表操作没有相关索引 建议优化\n");

    private String code;
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
