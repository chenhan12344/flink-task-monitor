package com.demo.model.bdp;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:06
 */

public class BDPConst {


    // public static final List<String> FLINK_WARN_GROUPS = Collections.singletonList("16083150");
    public static final List<String> FLINK_WARN_GROUPS = Collections.singletonList("16152425");
    public static String TASK_RUNNING_STATE = "RUNNING";
    /**
     * 慢FullGC时长阈值（秒）
     */
    public static int DEFAULT_SLOW_FULL_GC_THRESHOLD_SECONDS = 5;
    /**
     * 频繁FullGC阈值（次/小时）
     */
    public static int DEFAULT_FREQUENT_FULL_GC_THRESHOLD_PER_HOUR = 15;
    public static int CHECKPOINT_FAILED_DELTA_THRESHOLD = 2;
    /**
     * 慢CK时长阈值（毫秒）
     */
    public static long SLOW_CK_THRESHOLD_MILLISECONDS = TimeUnit.MINUTES.toMillis(5L);
    public static int oempAppID = 258;
    public static int rdmpAppID = 1097;

    public static final String[] IGNORE_ERRS = {"Heartbeat of TaskManager", "Exceeded checkpoint tolerable failure threshold", "remote task manager was lost"};
}
