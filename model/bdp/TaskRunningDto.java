package com.demo.model.bdp;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:17
 */
@Data
public class TaskRunningDto implements Serializable {
    private static final long serialVersionUID = 4155289437525347363L;
    /**
     * BDP Flink任务ID
     */
    private Integer taskId;
    /**
     * Flink任务类型
     */
    private Integer taskType;
    /**
     * 当前运行版本号
     */
    private Integer currentVersionId;
    /**
     * Yarn集群ID
     */
    private Integer clusterId;
    /**
     * Yarn集群名称
     */
    private String clusterName;
    /**
     * Flink作业ID
     */
    private String jobId;
    /**
     * Yarn作业ID
     */
    private String applicationId;
    /**
     * Flink Dashboard地址
     */
    private String taskUrl;
    /**
     * Flink任务状态
     * LAUNCHING：启动中
     * RUNNING：运行中
     * STOPPING：停止中
     * RUN_FAILED：启动失败
     * KILLED：已停止
     */
    private String runStatus;
    /**
     *
     */
    private String host;
    /**
     * TaskManager数量
     */
    private Integer tmNum;
    /**
     * TaskManager内存大小（MB）
     */
    private Integer tmMem;
    /**
     * TaskManager CPU数量
     */
    private Integer tmCore;
    /**
     * JobManager CPU数量（正常情况下都是1）
     */
    private Integer jmCore;
    /**
     * JobManager内存大小（MB）
     */
    private Integer jmMem;
    /**
     * 是否积压
     */
    private boolean overstock;
    /**
     * 启动时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Long launchTime;

    public String getExceptionUrl() {
        return taskUrl + "jobs/" + jobId + "/exceptions?maxExceptions=1";
    }
}
