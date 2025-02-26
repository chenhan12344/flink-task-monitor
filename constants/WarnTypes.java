package com.demo.constants;

/**
 * @author sky
 */
public enum WarnTypes {

    COOKIE_EXPIRED("BDP Cookie过期"),
    EXCEPTION("异常报错"),
    SLOW_GC("GC慢"),
    FREQUENT_FULL_GC("GC频繁"),
    CONTINUOUS_CHECKPOINT_FAILING("CK连续失败"),
    SAVEPOINT_FAILED("SP失败"),
    CHECKPOINT_SLOW("CK/SP慢"),
    CK_UN_TRIGGER("CK/SP未触发"),
    FREQUENT_RESTORING("内部拉起"),
    DATA_SKEW("数据倾斜"),
    CONTAINER_PHYS_MEM_REACH_CRITICAL_LEVEL("物理内存阈值告警"),
    CONTAINER_EXCEED_PHYS_MEM_LIMIT("容器内存超用被Kill"),
    TASK_MANAGER_LOST("TaskManager重启");


    private final String desc;

    WarnTypes(String desc) {
        this.desc = desc;

    }

    public String getDesc() {
        return desc;
    }
}
