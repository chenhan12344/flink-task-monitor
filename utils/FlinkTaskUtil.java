package com.demo.utils;

import com.demo.model.TaskEnv;
import com.demo.sink.VoidSink;
import com.demo.source.FlinkAuxSource;
import com.demo.state.BloomFilterEnabledBlockBasedOptionsFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ResourceManagerOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.contrib.streaming.state.RocksDBNativeMetricOptions;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Preconditions;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author sky
 */
public class FlinkTaskUtil {

    public static final String ENVIRONMENT_VARIABLE_PROFILE = "TASK_PROFILE";
    public static final String ENVIRONMENT_VARIABLE_TASK_ID = "TASK_ID";
    public static final String ENVIRONMENT_VARIABLE_IGNORE_ILLEGAL_SOURCE_DATA = "IGNORE_ILLEGAL_SOURCE_DATA";

    public static final String STATE_BACKEND_ROCKS_DB = "RocksDB";
    public static final String STATE_BACKEND_HASHMAP = "HashMap";

    public static final String BDP_TASK_ID = "rtcTaskId";
    public static final String BDP_CK_DIR = "ckDir";
    public static final String BDP_CK_INTERVAL = "ckInterval";
    public static final String BDP_PARALLELISM = "parallelism";

    public static final String USER_PROFILE = "profile";
    public static final String USER_TASK_NAME = "taskName";
    public static final String USER_CK_MIN_PAUSE_SECONDS = "ckMinPauseSeconds";
    public static final String USER_STATE_BACKEND_TYPE = "stateBackendType";
    public static final String USER_CK_TIMEOUT_MINUTES = "ckTimeoutMinutes";
    public static final String LOCAL_CK_DRI = "file:///tmp";

    public static final String CONFIG_IGNORE_ILLEGAL_SOURCE_DATA = "ignoreIllegalSourceData";
    public static final String CONFIG_LOCAL_ENV_PORT = "localEnvPort";
    public static final String CONFIG_USE_LOCAL_ENV = "useLocalEnv";
    public static final String CONFIG_KEY_ENABLE_CHECKPOINTING = "enableCheckpointing";
    public static final String CONFIG_KEY_ENABLE_INCREMENTAL_CHECKPOINTING = "enableIncrementalCheckpointing";
    public static final String CONFIG_KEY_ENABLE_PROCESS_MONITORING = "enableProcessMonitoring";
    public static final String CONFIG_KEY_ENABLE_ROCKSDB_BLOOM_FILTER = "enableRocksDBBloomFilter";
    public static final String CONFIG_KEY_ENABLE_ROCKSDB_NATIVE_METRICS_UPLOAD = "enableRocksDBNativeMetricsUpload";

    public static TaskEnv configureAndValidateEnv(String[] args) throws Exception {
        // 环境校验
        TaskEnv taskEnv = validateEnv(args);
        ParameterTool params = taskEnv.getParameterTool();

        String profile = taskEnv.getProfile();
        Preconditions.checkArgument(StringUtils.isNotBlank(profile), "main运行环境未指定\n参数示例: --profile dev --taskId 123456 --taskName LocalEnvTest");
        System.setProperty(ENVIRONMENT_VARIABLE_PROFILE, profile);

        // Flink运行环境配置
        StreamExecutionEnvironment executionEnvironment;
        Configuration configuration = new Configuration();
        configuration.setBoolean(CoreOptions.CHECK_LEAKED_CLASSLOADER, false);

        // 是否开启RocksDB监控指标上传到Grafana（默认开启）
        if (params.getBoolean(CONFIG_KEY_ENABLE_ROCKSDB_NATIVE_METRICS_UPLOAD, true)) {
            configureRocksDBNativeMetricsMonitorOptions(configuration);
        }

        if (taskEnv.isDev() || params.getBoolean(CONFIG_USE_LOCAL_ENV, false)) {
            configuration.setInteger(RestOptions.PORT, params.getInt(CONFIG_LOCAL_ENV_PORT, 8089));
            executionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);
        } else {
            // 通过containerized.taskmanager.env参数传递给TaskManager容器的环境变量，这样可以在TaskManager使用System.getEnv()获取全局环境变量
            configuration.setString(ResourceManagerOptions.CONTAINERIZED_TASK_MANAGER_ENV_PREFIX + ENVIRONMENT_VARIABLE_PROFILE, profile);
            configuration.setString(ResourceManagerOptions.CONTAINERIZED_TASK_MANAGER_ENV_PREFIX + ENVIRONMENT_VARIABLE_TASK_ID, taskEnv.getTaskId());
            configuration.setString(ResourceManagerOptions.CONTAINERIZED_TASK_MANAGER_ENV_PREFIX + ENVIRONMENT_VARIABLE_IGNORE_ILLEGAL_SOURCE_DATA, String.valueOf(params.getBoolean(CONFIG_IGNORE_ILLEGAL_SOURCE_DATA, false)));
            executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        }
        taskEnv.setExecutionEnvironment(executionEnvironment);

        // 设置作业全局并行度（随BDP启动策略并行度变化，方便随时调整）
        int globalParallelism = Integer.parseInt(System.getProperty(BDP_PARALLELISM, "1"));
        taskEnv.setGlobalParallelism(globalParallelism);
        executionEnvironment.setParallelism(globalParallelism);

        // 禁止自动生成UID，所有算子必须手动指定UID，避免忘记指定UID后因拓扑改变导致算子状态无法恢复
        if (!taskEnv.isDev()) {
            executionEnvironment.getConfig().disableAutoGeneratedUIDs();
        }

        executionEnvironment.getConfig().setGlobalJobParameters(params);

        // Checkpoint配置
        boolean enableCheckpointing = params.getBoolean(CONFIG_KEY_ENABLE_CHECKPOINTING, true);
        if (enableCheckpointing) {
            // Checkpoint基本配置
            CheckpointConfig checkpointConfig = executionEnvironment.getCheckpointConfig();

            String ckDir = taskEnv.isDev() ? LOCAL_CK_DRI : System.getProperty(BDP_CK_DIR);
            Preconditions.checkArgument(StringUtils.isNotBlank(ckDir), "ckDir为空");
            checkpointConfig.setCheckpointStorage(new FileSystemCheckpointStorage(ckDir));
            checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

            checkpointConfig.setCheckpointInterval(TimeUnit.SECONDS.toMillis(Long.parseLong(System.getProperty(BDP_CK_INTERVAL, "120"))));
            checkpointConfig.setCheckpointTimeout(TimeUnit.MINUTES.toMillis(params.getLong(USER_CK_TIMEOUT_MINUTES, 10L)));
            checkpointConfig.setMinPauseBetweenCheckpoints(TimeUnit.SECONDS.toMillis(params.getLong(USER_CK_MIN_PAUSE_SECONDS, 120L)));
            checkpointConfig.setMaxConcurrentCheckpoints(1);

            // 状态后端配置
            String stateBackendType = params.get(USER_STATE_BACKEND_TYPE, STATE_BACKEND_ROCKS_DB);
            if (STATE_BACKEND_ROCKS_DB.equalsIgnoreCase(stateBackendType)) {
                executionEnvironment.setStateBackend(configureRocksDBStateBackend(params));
            } else if (STATE_BACKEND_HASHMAP.equals(stateBackendType)) {
                executionEnvironment.setStateBackend(new HashMapStateBackend());
            } else {
                throw new RuntimeException("不支持的StateBackend类型：" + stateBackendType);
            }

            // 任务拉起间隔配置，使用指数增长间隔，每次拉起后等待时间翻倍才会再次触发拉起，避免拉起过于频繁导致异常被淹没
            executionEnvironment.setRestartStrategy(RestartStrategies.exponentialDelayRestart(
                    Time.seconds(2L),   // 初始拉起间隔
                    Time.hours(1L),     // 最大拉起间隔
                    2.0,                // 拉起间隔增长指数
                    Time.hours(1L),     // 拉起间隔回退至初始值的正常持续运行时间
                    0.0                 // 拉起间隔浮动因子（拉起间隔会正负因子百分比内范围浮动，一般用于避免多个作业同时拉起，这里可以不用）
            ));

        } else {
            executionEnvironment.getCheckpointConfig().disableCheckpointing();
        }

        // 是否启用JVM进程监控
        boolean enableProcessMonitoring = params.getBoolean(CONFIG_KEY_ENABLE_PROCESS_MONITORING, true);
        if (enableProcessMonitoring) {
            // 这里的并行度应当为整个任务最大的并行度，这样才能确保算子每个TaskManager都能被分配到，否则只能监控一部分TaskManger
            int parallelism = executionEnvironment.getParallelism();
            executionEnvironment.addSource(new FlinkAuxSource())
                    .name("FlinkAuxSource")
                    .uid("FlinkAuxSource")
                    .setParallelism(parallelism)
                    .addSink(new VoidSink<>())
                    .name("VoidSink")
                    .uid("VoidSink")
                    .setParallelism(parallelism);
        }
        return taskEnv;
    }

    /**
     * 配置RocksDB Native指标监控
     *
     * @param configuration
     */
    private static void configureRocksDBNativeMetricsMonitorOptions(Configuration configuration) {
        configuration.setBoolean(RocksDBNativeMetricOptions.ESTIMATE_PENDING_COMPACTION_BYTES, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_DELETES_ACTIVE_MEM_TABLE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_ENTRIES_ACTIVE_MEM_TABLE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.ESTIMATE_TABLE_READERS_MEM, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_DELETES_IMM_MEM_TABLE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_ENTRIES_IMM_MEM_TABLES, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_ACTUAL_DELAYED_WRITE_RATE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_CUR_SIZE_ACTIVE_MEM_TABLE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.BLOCK_CACHE_PINNED_USAGE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_CUR_SIZE_ALL_MEM_TABLE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.ESTIMATE_LIVE_DATA_SIZE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_MEM_TABLE_FLUSH_PENDING, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_IMMUTABLE_MEM_TABLES, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_RUNNING_COMPACTIONS, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.BLOCK_CACHE_CAPACITY, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_TOTAL_SST_FILES_SIZE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_RUNNING_FLUSHES, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_SIZE_ALL_MEM_TABLES, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.TRACK_COMPACTION_PENDING, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_BACKGROUND_ERRORS, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.BLOCK_CACHE_USAGE, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.ESTIMATE_NUM_KEYS, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_LIVE_VERSIONS, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.IS_WRITE_STOPPED, true);
        configuration.setBoolean(RocksDBNativeMetricOptions.MONITOR_NUM_SNAPSHOTS, true);
    }

    /**
     * 配置RocksDB状态后端
     *
     * @param params 任务参数
     * @return RocksDB状态后端
     */
    private static EmbeddedRocksDBStateBackend configureRocksDBStateBackend(ParameterTool params) {
        // 是否启用增量CK
        boolean enableIncrementalCheckpointing = params.getBoolean(CONFIG_KEY_ENABLE_INCREMENTAL_CHECKPOINTING, true);
        EmbeddedRocksDBStateBackend backend = new EmbeddedRocksDBStateBackend(enableIncrementalCheckpointing);

        // 是否启用布隆过滤器
        boolean enableBloomFilter = params.getBoolean(CONFIG_KEY_ENABLE_ROCKSDB_BLOOM_FILTER, false);
        if (enableBloomFilter) {
            backend.setRocksDBOptions(new BloomFilterEnabledBlockBasedOptionsFactory());
        }

        return backend;
    }

    private static TaskEnv validateEnv(String[] args) {

        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        String actualTaskId = System.getProperty(BDP_TASK_ID);
        String profile = parameterTool.get(USER_PROFILE);
        String taskName = parameterTool.get(USER_TASK_NAME);
        Preconditions.checkNotNull(profile, "运行环境未指定\n参数示例: --profile dev --taskId 123456 --taskName LocalEnvTest");

        TaskEnv taskEnv = new TaskEnv(profile, actualTaskId);
        taskEnv.setTaskName(taskName);
        taskEnv.setParameterTool(parameterTool);
        return taskEnv;
    }

    /**
     * 获取当前生效的profile（任务运行环境）
     *
     * @return 当前生效的profile
     */
    public static String getProfile() {
        String systemPropertyProfile = System.getProperty(FlinkTaskUtil.ENVIRONMENT_VARIABLE_PROFILE);
        String systemEnvProfile = System.getenv(FlinkTaskUtil.ENVIRONMENT_VARIABLE_PROFILE);
        return Optional.ofNullable(systemPropertyProfile).orElse(systemEnvProfile);
    }

    /**
     * 获取任务ID
     *
     * @return 任务ID
     */
    public static String getTaskId() {
        String systemPropertyProfile = System.getProperty(FlinkTaskUtil.ENVIRONMENT_VARIABLE_TASK_ID);
        String systemEnvProfile = System.getenv(FlinkTaskUtil.ENVIRONMENT_VARIABLE_TASK_ID);
        return Optional.ofNullable(systemPropertyProfile).orElse(systemEnvProfile);
    }

    /**
     * 是否忽略脏数据
     *
     * @return
     */
    public static boolean ignoreIllegalSourceData() {
        String systemPropertyValue = System.getProperty(FlinkTaskUtil.ENVIRONMENT_VARIABLE_IGNORE_ILLEGAL_SOURCE_DATA);
        String systemEnvValue = System.getenv(FlinkTaskUtil.ENVIRONMENT_VARIABLE_IGNORE_ILLEGAL_SOURCE_DATA);
        return "true".equals(Optional.ofNullable(systemPropertyValue).orElse(systemEnvValue));
    }
}
