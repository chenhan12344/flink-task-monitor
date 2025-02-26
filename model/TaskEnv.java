package com.demo.model;

import com.demo.constants.EnvEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 任务执行环境Wrapper类
 *
 * @author sky
 */
@Getter
@Setter
public class TaskEnv {

    /**
     * 任务运行配置环境
     */
    private final String profile;
    /**
     * 任务ID
     */
    private final String taskId;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 全局并行度
     */
    private Integer globalParallelism;
    /**
     * 任务参数
     */
    private ParameterTool parameterTool;
    /**
     * Flink执行环境
     */
    private StreamExecutionEnvironment executionEnvironment;

    public TaskEnv(String profile, String taskId) {
        this.profile = profile;
        this.taskId = taskId;
    }

    public boolean isDev() {
        return EnvEnum.DEV.equalsIgnoreCase(profile);
    }

    public boolean isSit() {
        return EnvEnum.SIT.equalsIgnoreCase(profile);
    }

    public boolean isPar() {
        return EnvEnum.PAR.equalsIgnoreCase(profile);
    }

    public boolean isProd() {
        return EnvEnum.PROD.equalsIgnoreCase(profile);
    }

    public String getJobName() {
        return taskId + "_" + taskName + "_" + profile;
    }
}
