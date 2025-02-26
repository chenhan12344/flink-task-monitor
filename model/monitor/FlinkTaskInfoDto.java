package com.demo.model.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * Flink任务基本信息
 *
 * @author sky
 */
@Data
public class FlinkTaskInfoDto implements Serializable {

    /**
     * BDP应用ID
     */
    private Integer appId;

    /**
     * BDP应用名称
     */
    private String appName;

    /**
     * BDP任务ID
     */
    private Integer taskId;

    /**
     * BDP任务名称
     */
    private String taskName;

    /**
     * BDP任务描述
     */
    private String taskDescription;

    /**
     * 任务负责人工号
     */
    private String ownerId;

    /**
     * 任务负责人名称
     */
    private String ownerName;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 任务所在Yarn集群
     */
    private String clusterName;

    /**
     * 任务启动时间戳
     */
    private Long launchTime;

    /**
     * FlinkDashboard地址前缀
     * 格式形如：http://xxxxxx.xxx/application_xxx/
     */
    private String flinkDashboardUrlPrefix;

    /**
     * TaskManager内存大小（MB）
     */
    private Integer tmMem;

    /**
     * TaskManager数量
     */
    private Integer tmNum;

    /**
     * Flink作业ID
     */
    private String jobId;

    /**
     * 任务启动至今的运行时长（毫秒）
     */
    private Long totalRunningDuration;

    /**
     * 任务最近一次拉起至今的运行时长（毫秒）
     */
    private Long currentRunningDuration;

}
