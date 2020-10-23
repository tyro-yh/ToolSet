package com.batchtask.manage.constants;

/**
 * 任务状态枚举常量
 * @author yh
 */
public enum  StatusEnum {
    /**
     * 初始化
     */
    INIT("初始化","1"),
    /**
     * 执行中
     */
    IN_EXECUTION("执行中","2"),
    /**
     * 执行完毕
     */
    COMPLETE("执行完毕","3"),
    /**
     * 失败需重试
     */
    FAIL_RETRY("失败需重试","4"),
    /**
     * 失败无需重试
     */
    FAIL_END("失败无需重试","5");
    /**
     * 任务状态名
     */
    private String name;
    /**
     * 任务状态值
     */
    private String value;

    StatusEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() { return value; }

    public void setValue(String value) {
        this.value = value;
    }

    public static StatusEnum getEnumByValue(String value) {
        for (StatusEnum se : StatusEnum.values()) {
            if (se.getValue().equals(value)) {
                return se;
            }
        }
        return null;
    }
}
