package com.demo.model;

import com.google.common.collect.ImmutableSet;
import com.demo.model.bdp.BDPConst;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 监控配置类
 *
 * @author sky
 */
@Data
public class MonitorConfigurations implements Serializable {

    /**
     * 是否启用物理内存检查
     */
    private final boolean enablePhysicalMemoryMonitoring;

    /**
     * 是否启用GC检查
     */
    private final boolean enableGcMonitoring;

    /**
     * 是否启用CK/SP检查
     */
    private final boolean enableCheckpointMonitoring;

    /**
     * 是否启用异常检查
     */
    private final boolean enableExceptionMonitoring;

    /**
     * 是否启用容器内存超用检查
     */
    private final boolean enableContainerKilledMonitoring;

    /**
     * 是否启用数据倾斜检查
     */
    private final boolean enableDataSkewMonitoring;

    /**
     * 任务检查黑名单（不检测的任务）
     */
    private final Set<String> taskIdBlacklist;
    /**
     * 数据倾斜任务黑名单（不检测数据倾斜的任务）
     */
    private final Set<String> skewBlacklist;
    /**
     * 数据倾斜判定阈值
     */
    private final double skewRatio;

    /**
     * 物理内存告警阈值
     */
    private final long physicalMemoryMBWarnThreshold;
    /**
     * 物理内存告警间隔
     */
    private final int physicalMemoryWarnInterval;

    /**
     * GC检查间隔（分钟）
     */
    private final int gcCheckIntervalMinutes;

    /**
     * 慢GC阈值（秒）
     */
    private final int slowGcThresholdSeconds;

    /**
     * 频繁GC阈值（次/小时）
     */
    private final int frequentGcThresholdPerHour;

    /**
     * 慢CK阈值（毫秒）
     */
    private final long slowCheckpointThresholdMs;

    /**
     * 从任务参数里面读取配置信息
     */
    public MonitorConfigurations(ParameterTool parameterTool) {
        this.enablePhysicalMemoryMonitoring = parameterTool.getBoolean("enablePhysicalMemoryMonitoring", true);
        this.enableGcMonitoring = parameterTool.getBoolean("enableGcMonitoring", true);
        this.enableCheckpointMonitoring = parameterTool.getBoolean("enableCheckpointMonitoring", true);
        this.enableExceptionMonitoring = parameterTool.getBoolean("enableExceptionMonitoring", true);
        this.enableContainerKilledMonitoring = parameterTool.getBoolean("enableContainerKilledMonitoring", true);
        this.enableDataSkewMonitoring = parameterTool.getBoolean("enableDataSkewMonitoring", true);

        String taskIdBlacklist = parameterTool.get("taskIdBlacklist");
        this.taskIdBlacklist = StringUtils.isNotBlank(taskIdBlacklist) ? ImmutableSet.copyOf(taskIdBlacklist.split(",")) : Collections.emptySet();

        String skewBlacklist = parameterTool.get("skewBlacklist");
        this.skewBlacklist = StringUtils.isNotBlank(skewBlacklist) ? ImmutableSet.copyOf(skewBlacklist.split(",")) : Collections.emptySet();

        this.skewRatio = parameterTool.getDouble("skewRatio", 1.0);
        this.physicalMemoryMBWarnThreshold = parameterTool.getLong("physicalMemoryMBWarnThreshold", 100L);
        this.physicalMemoryWarnInterval = parameterTool.getInt("physicalMemoryWarnInterval", 5);
        this.gcCheckIntervalMinutes = parameterTool.getInt("gcCheckIntervalMinutes", 5);
        this.slowGcThresholdSeconds = parameterTool.getInt("slowGcThresholdSeconds", BDPConst.DEFAULT_SLOW_FULL_GC_THRESHOLD_SECONDS);
        this.frequentGcThresholdPerHour = parameterTool.getInt("frequentGcCountThreshold", BDPConst.DEFAULT_FREQUENT_FULL_GC_THRESHOLD_PER_HOUR);
        this.slowCheckpointThresholdMs = TimeUnit.MINUTES.toMillis(parameterTool.getLong("slowCheckpointThresholdMinutes", 5L));
    }
}
