package com.demo.model.monitor;

import com.demo.model.flinkdashboard.GarbageCollectDto;
import com.demo.model.flinkdashboard.checkpoint.CheckpointCounts;
import lombok.Data;
import org.apache.commons.collections.map.HashedMap;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author sky
 */
@Data
public class TaskMonitorState implements Serializable {

    /**
     * BDP任务ID
     */
    private int taskId;

    /**
     * 任务启动时间
     */
    private long launchTime;

    /**
     * TM异常时间戳
     */
    private long taskManagerExceptionTimestamp;

    /**
     * TM容器被杀死的时间戳
     */
    private long containerKilledTimestamp;

    /**
     * 任务启动至今的运行时长（毫秒）
     */
    private long totalRunningDuration;

    /**
     * 任务最近一次拉起至今的运行时长（毫秒）
     */
    private long currentRunningDuration;

    /**
     * CK统计信息
     */
    private CheckpointCounts checkpointCounts;

    /**
     * 最近一次CK失败的时间戳
     */
    private long checkpointFailedTimestamp;

    /**
     * 最近一次SP失败的时间戳
     */
    private long savepointFailedTimestamp;

    /**
     * GC信息
     */
    private Map<String, GarbageCollectDto> fullGc;

    /**
     * 容器物理内存达到警戒水位时间戳
     */
    private long physicalMemoryReachCriticalLevelTimestamp;

    /**
     * GC检查时间戳
     */
    private long gcCheckedTimestamp;

    /**
     * 告警时间记录
     */
    private Map<Integer, Date> errMsgCache = new HashedMap();
}
