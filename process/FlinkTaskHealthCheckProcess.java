package com.demo.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.demo.state.PrototsuffValueState;
import com.demo.utils.FlinkTaskUtil;
import com.demo.utils.ParameterToolFetcher;
import com.demo.utils.DateUtil;
import com.demo.utils.FlinkDashboardUtil;
import com.demo.constants.WarnTypes;
import com.demo.model.MonitorConfigurations;
import com.demo.model.bdp.BDPConst;
import com.demo.model.bdp.FlinkWarnMsgDto;
import com.demo.model.bdp.JobDto;
import com.demo.model.bdp.TaskRunningDto;
import com.demo.model.flinkdashboard.*;
import com.demo.model.flinkdashboard.checkpoint.CheckpointCounts;
import com.demo.model.flinkdashboard.checkpoint.CheckpointDetail;
import com.demo.model.flinkdashboard.checkpoint.CheckpointInfo;
import com.demo.model.monitor.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sky
 */
@Slf4j
public class FlinkTaskHealthCheckProcess extends KeyedProcessFunction<Integer, FlinkTaskInfoDto, FlinkWarnMsgDto> {

    private static final int SKEW_MIN_COUNT = 10000000;
    private static final int SKEW_MIN_PRESSURE = 90;
    private static final long HOUR_MS = 60 * 60 * 1000;

    public static final Pattern TM_LOST_EXCEPTION_PATTERN =
            Pattern.compile("^org\\.apache\\.flink\\.runtime\\.io\\.network\\.netty\\.exception\\.RemoteTransportException: Connection unexpectedly closed by remote task manager '([0-9a-z]+/\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+)'");


    /**
     * 监控配置
     */
    private MonitorConfigurations monitorConfigurations;


    private transient ValueState<TaskMonitorState> taskMonitorValueState;

    @Override
    public void open(Configuration parameters) throws Exception {

        ValueStateDescriptor<byte[]> taskMonitorStateDesc = PrototsuffValueState.descriptor("taskMonitorState");
        taskMonitorStateDesc.enableTimeToLive(StateTtlConfig.newBuilder(Time.days(14L))
                .neverReturnExpired()
                .cleanupFullSnapshot()
                .build());
        taskMonitorValueState = new PrototsuffValueState<>(getRuntimeContext().getState(taskMonitorStateDesc), TaskMonitorState.class);

        ParameterTool parameterTool = ParameterToolFetcher.fetch(this);
        this.monitorConfigurations = new MonitorConfigurations(parameterTool);
    }

    @Override
    public void processElement(FlinkTaskInfoDto taskInfo, Context ctx, Collector<FlinkWarnMsgDto> out) throws Exception {
        int taskId = taskInfo.getTaskId();
        if (taskId == 0) {
            FlinkWarnMsgDto cookieExpiredWarn = new FlinkWarnMsgDto();
            cookieExpiredWarn.setAppId(null);
            cookieExpiredWarn.setTaskId(Integer.parseInt(FlinkTaskUtil.getTaskId()));
            cookieExpiredWarn.setTaskName("Flink任务健康巡检");
            cookieExpiredWarn.setOwnerId("-");
            cookieExpiredWarn.setOwnerName("-");
            cookieExpiredWarn.setCurrentDuration(0L);
            cookieExpiredWarn.setTaskDuration(0L);
            cookieExpiredWarn.setClusterName("-");
            cookieExpiredWarn.addWarnInfo(new CookieExpiredInfo());
            out.collect(cookieExpiredWarn);
            return;
        }

        if (monitorConfigurations.getTaskIdBlacklist().contains(String.valueOf(taskId))) {
            log.info("发现黑名单任务,跳过检查：{}", taskId);
            return;
        }

        TaskMonitorState taskMonitorState = taskMonitorValueState.value();
        // 新任务或者任务被重启后需要清空任务状态
        if (taskMonitorState == null || !Objects.equals(taskMonitorState.getLaunchTime(), taskInfo.getLaunchTime())) {
            taskMonitorState = new TaskMonitorState();
            taskMonitorState.setTaskId(ctx.getCurrentKey());
            taskMonitorState.setLaunchTime(taskInfo.getLaunchTime());
        }


        String flinkDashboardUrlPrefix = taskInfo.getFlinkDashboardUrlPrefix();
        JobDto jobDto = FlinkDashboardUtil.getJobsOverview(flinkDashboardUrlPrefix);
        if (jobDto == null) {
            return;
        }
        String clusterName = taskInfo.getSystemCode().split("-")[1] + "|" + taskInfo.getClusterName().replace("实时计算Yarn", "");

        JobOverviewDto jobOverview = FlinkDashboardUtil.getJobOverview(flinkDashboardUrlPrefix, jobDto.getJid());
        taskInfo.setTotalRunningDuration(jobOverview.getDuration());
        taskInfo.setCurrentRunningDuration(jobOverview.getTimestamps().getRunning());

        FlinkWarnMsgDto errMsgDto = new FlinkWarnMsgDto();
        errMsgDto.setAppId(taskInfo.getAppId());
        errMsgDto.setTaskId(taskId);
        errMsgDto.setTaskName(taskInfo.getTaskName());
        errMsgDto.setOwnerId(taskInfo.getOwnerId());
        errMsgDto.setOwnerName(taskInfo.getOwnerName());
        errMsgDto.setCurrentDuration(System.currentTimeMillis() - jobOverview.getTimestamps().getRunning());
        errMsgDto.setTaskDuration(jobOverview.getDuration());
        errMsgDto.setClusterName(clusterName);
        errMsgDto.setFlinkDashboardLink(taskInfo.getFlinkDashboardUrlPrefix() + "index.html");


        // 检查TM异常报错信息
        if (monitorConfigurations.isEnableExceptionMonitoring()) {
            WarnInfo warnInfo = checkTaskManagerExceptions(taskInfo, taskMonitorState);
            if (warnInfo instanceof TaskManagerLostInfo) {
                errMsgDto.addWarnInfo(warnInfo);
            }
            if (warnInfo instanceof ExceptionInfo) {
                ExceptionInfo exceptionInfo = (ExceptionInfo) warnInfo;
                if (frequencyCheck(taskMonitorState, 1, WarnTypes.EXCEPTION.getDesc() + exceptionInfo.getDirectException())) {
                    errMsgDto.addWarnInfo(warnInfo);
                }
            }
        }

        // 检查JM异常报错信息
        if (monitorConfigurations.isEnableContainerKilledMonitoring()) {
            WarnInfo jmExceptionInfo = checkJobManagerExceptions(taskInfo, taskMonitorState);
            if (jmExceptionInfo != null) {
                errMsgDto.addWarnInfo(jmExceptionInfo);
            }
        }

        // 检查GC频繁\超时
        if (monitorConfigurations.isEnableGcMonitoring()) {
            List<WarnInfo> gcWarnInfos = checkGC(taskInfo, taskMonitorState);
            if (!gcWarnInfos.isEmpty() && frequencyCheck(taskMonitorState, 1, WarnTypes.FREQUENT_FULL_GC.getDesc())) {
                errMsgDto.addWarnInfos(gcWarnInfos);
            }
        }

        // 检查CK\SP超时、失败、任务拉起次数
        if (monitorConfigurations.isEnableCheckpointMonitoring()) {
            List<WarnInfo> ckWarnInfos = checkCK(taskInfo, taskMonitorState);
            if (!ckWarnInfos.isEmpty()) {
                errMsgDto.addWarnInfos(ckWarnInfos);
            }
        }

        // 检查数据倾斜，由于数据倾斜是瞬时状态，所以不需要和历史状态对比
        if (monitorConfigurations.isEnableDataSkewMonitoring()) {
            WarnInfo dataSkewInfo = checkDataSkew(taskInfo);
            if (dataSkewInfo != null) {
                errMsgDto.addWarnInfo(dataSkewInfo);
            }
        }

        // 检查物理内存使用情况（如果任务支持）
        if (monitorConfigurations.isEnablePhysicalMemoryMonitoring()) {
            WarnInfo physicalMemoryUsageInfo = checkPhysicalMemoryUsage(taskInfo, taskMonitorState);
            if (physicalMemoryUsageInfo != null) {
                errMsgDto.addWarnInfo(physicalMemoryUsageInfo);
            }
        }

        // 检查BDP告警开关长时间关闭
//        checkBDPLongTimeAlarmOff(taskRunning, errMsgDto);


        if (!errMsgDto.getWarnInfos().isEmpty()) {
            out.collect(errMsgDto);
        }

        taskMonitorState.setCurrentRunningDuration(taskInfo.getCurrentRunningDuration());
        taskMonitorState.setTotalRunningDuration(taskInfo.getTotalRunningDuration());
        taskMonitorValueState.update(taskMonitorState);
    }


    private WarnInfo checkJobManagerExceptions(FlinkTaskInfoDto taskInfo, TaskMonitorState taskMonitorState) throws Exception {
        String flinkDashboardUrlPrefix = taskInfo.getFlinkDashboardUrlPrefix();
        LogInfo logInfo = FlinkDashboardUtil.getJobManagerLogs(flinkDashboardUrlPrefix);

        if (logInfo == null) {
            return null;
        }

        for (LogInfo.LogFile logFile : logInfo.getLogs()) {
            // 只检查最新的JM日志文件，历史文件名格式为：jobmanager.log.*
            if (!"jobmanager.log".equals(logFile.getName())) {
                continue;
            }

            // 超过10MB的日志文件会有性能问题不再检查
            long logFileSizeMB = (logFile.getSize() >> 20);
            if (logFileSizeMB > 10L) {
                return null;
            }
        }

        String logText = FlinkDashboardUtil.getJobManagerLog(taskInfo.getFlinkDashboardUrlPrefix(), taskInfo.getJobId());
        if (logText == null) {
            return null;
        }

        String[] logLineArray = logText.split("\\n");

        // 倒序遍历日志
        for (int i = logLineArray.length - 1; i > 0; i--) {
            String logStr = logLineArray[i];
            if (!logStr.startsWith("20")) {
                continue;
            }

            if (!logStr.endsWith("Killing container.")) {
                continue;
            }

            Date logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").parse(logStr.substring(0, 23));
            long currentKilledTimestamp = logTime.getTime();
            long prevKilledTimestamp = taskMonitorState.getContainerKilledTimestamp();
            // 和之前的状态对比，如果发生时间戳一致则表明没有新增
            if (currentKilledTimestamp == prevKilledTimestamp) {
                return null;
            }

            // 更新状态时间
            taskMonitorState.setContainerKilledTimestamp(currentKilledTimestamp);

            ContainerExceedPhysicalMemoryLimitInfo info = new ContainerExceedPhysicalMemoryLimitInfo();
            info.setTimestamp(currentKilledTimestamp);
            info.setLogInfo(trimLog(logStr));
            return info;
        }

        return null;
    }

    private static String trimLog(String logStr) {
        return logStr.substring(0, 19) + " [" + logStr.split("containerID=")[1];
    }


    private WarnInfo checkDataSkew(FlinkTaskInfoDto taskInfo) {
        if (monitorConfigurations.getSkewBlacklist().contains(taskInfo.getTaskId().toString())) {
            return null;
        }

        String flinkDashboardUrlPrefix = taskInfo.getFlinkDashboardUrlPrefix();

        TaskGraphDto taskGraphDto = FlinkDashboardUtil.getTaskGraph(flinkDashboardUrlPrefix, taskInfo.getJobId());
        if (taskGraphDto == null) {
            return null;
        }
        double skewRatio = monitorConfigurations.getSkewRatio();

        //遍历算子
        for (VertexDto vertex : taskGraphDto.getVertices()) {
            long readRecords = vertex.getMetrics().getReadRecords();
            if (readRecords < SKEW_MIN_COUNT) {
                continue;
            }
            int parallelism = vertex.getParallelism();
            double recordsAvg = 1D * readRecords / parallelism;
            String slotId = vertex.getId();
            double avgPressure = getSlotAvgPressure(taskInfo, slotId);
            if (avgPressure <= SKEW_MIN_PRESSURE) {
                continue;
            }
            SubTaskMainDto subTaskMain = FlinkDashboardUtil.getSubTaskInfo(flinkDashboardUrlPrefix, taskInfo.getJobId(), slotId);
            if (subTaskMain == null) {
                continue;
            }
            //遍历subtask
            for (SubtaskDto subtaskDto : subTaskMain.getSubtasks()) {
                long curReadRecords = subtaskDto.getMetrics().getReadRecords();
                if (curReadRecords == 0) {
                    break;
                }
                if ((curReadRecords - recordsAvg) / recordsAvg > skewRatio) {
                    List<Long> recordList = new ArrayList<>();
                    long cnt = 0;
                    for (SubtaskDto sub : subTaskMain.getSubtasks()) {
                        recordList.add(sub.getMetrics().getReadRecords());
                        cnt += sub.getMetrics().getReadRecords();
                    }
                    if (cnt < SKEW_MIN_COUNT) {
                        break;
                    }

                    JobOverviewDto jobOverview = FlinkDashboardUtil.getJobOverview(flinkDashboardUrlPrefix, taskInfo.getJobId());
                    if (jobOverview.getTimestamps().getRunning() < HOUR_MS) continue;

                    DataSkewInfo dataSkewInfo = new DataSkewInfo();
                    dataSkewInfo.setOperatorName(vertex.getName());
                    dataSkewInfo.setOperatorPressure(avgPressure);
                    dataSkewInfo.setSkewThreshold(skewRatio);
                    dataSkewInfo.setRecordsReceived(recordList);
                    return dataSkewInfo;
                }
            }
        }

        return null;
    }

    private static Double getSlotAvgPressure(FlinkTaskInfoDto taskRunning, String slotId) {
        List<MathIndexDto> indexes = FlinkDashboardUtil.getSlotPressure(taskRunning.getFlinkDashboardUrlPrefix(), taskRunning.getJobId(), slotId);
        if (indexes == null || indexes.size() != 2) {
            return 0D;
        }
        MathIndexDto mathIndexDto = indexes.get(1);
        double avgPressure = mathIndexDto.getAvg() / 10;
        // Double avgPressureFirst = mathIndexDto.getMax() / 10;
        // if (avgPressure > 50) {
        //     try {
        //         Thread.sleep(1000);
        //     } catch (InterruptedException e) {
        //         throw new RuntimeException(e);
        //     }
        //     List<MathIndexDto> indexesRecheck = FlinkMetricsUtil.getSlotPressure(taskRunning.getTaskUrl(), taskRunning.getJobId(), slotId);
        //     if (indexesRecheck == null || indexesRecheck.size() != 2) {
        //         return 0D;
        //     }
        //     avgPressure = indexesRecheck.get(1).getMax() / 10;
        //     log.info("{}首次负载:{} 复核负载:{}", taskRunning.getTaskId(), avgPressureFirst, avgPressure);
        // }
        return avgPressure;
    }


    private void checkBDPLongTimeAlarmOff(TaskRunningDto taskRunning, FlinkWarnMsgDto errMsgDto) {

    }

    private List<WarnInfo> checkCK(FlinkTaskInfoDto taskInfo, TaskMonitorState taskMonitorState) {
        CheckpointInfo checkpointInfo = FlinkDashboardUtil.getCheckpoints(taskInfo.getFlinkDashboardUrlPrefix(), taskInfo.getJobId());
        if (checkpointInfo == null) {
            return Collections.emptyList();
        }

        CheckpointCounts stateCheckpointCounts = Optional.ofNullable(taskMonitorState.getCheckpointCounts()).orElse(new CheckpointCounts());
        CheckpointCounts currentCheckpointCounts = checkpointInfo.getCheckpointCounts();

        int currentFailed = currentCheckpointCounts.getFailed();
        int currentRestored = currentCheckpointCounts.getRestored();
        int deltaFailed = currentCheckpointCounts.getFailed() - stateCheckpointCounts.getFailed();
        int deltaRestored = currentCheckpointCounts.getRestored() - stateCheckpointCounts.getRestored();

        // 更新状态的GC统计信息
        taskMonitorState.setCheckpointCounts(currentCheckpointCounts);

        if (deltaFailed == 0 && deltaRestored == 0) {
            // CK失败次数或者拉起次数没有增加直接返回
            return Collections.emptyList();
        }

        List<CheckpointDetail> history = checkpointInfo.getCheckpointHistory();
        if (history.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarnInfo> warnInfos = new ArrayList<>();

        // 检查最新的CK是不是SP，并且是否失败
        CheckpointDetail latestCheckpoint = history.get(0);
        Long triggerTimestampLatest = latestCheckpoint.getTriggerTimestamp();

        if (latestCheckpoint.getIsSavepoint() && "FAILED".equals(latestCheckpoint.getStatus())) {
            long triggerTimestamp = taskMonitorState.getSavepointFailedTimestamp();
            if (triggerTimestamp != triggerTimestampLatest) {
                SavepointFailureInfo savepointFailureInfo = new SavepointFailureInfo();
                savepointFailureInfo.setDuration(latestCheckpoint.getEndToEndDuration());
                savepointFailureInfo.setTriggerTimestamp(triggerTimestampLatest);
                taskMonitorState.setSavepointFailedTimestamp(triggerTimestampLatest);
                warnInfos.add(savepointFailureInfo);
            }
        }

        // 慢CK检测
        if (isSlowCheckpoint(latestCheckpoint.getEndToEndDuration())) {
            CheckpointSlowInfo checkpointSlowInfo = new CheckpointSlowInfo();
            checkpointSlowInfo.setSavepoint(latestCheckpoint.getIsSavepoint());
            checkpointSlowInfo.setDuration(latestCheckpoint.getEndToEndDuration());
            warnInfos.add(checkpointSlowInfo);
        }

        // CK连续失败
        int continueFailCnt = getContinueFailCnt(history);
        if (continueFailCnt >= BDPConst.CHECKPOINT_FAILED_DELTA_THRESHOLD) {
            ContinuousCheckpointFailingInfo failingInfo = new ContinuousCheckpointFailingInfo();
            failingInfo.setContinuousFailedCount(continueFailCnt);
            failingInfo.setTriggerTimestamp(triggerTimestampLatest);
            failingInfo.setRecentFailedCheckpointDuration(latestCheckpoint.getEndToEndDuration());
            warnInfos.add(failingInfo);
        }

        // 任务发生拉起
        if (currentRestored > 1 && deltaRestored > 10 && frequencyCheck(taskMonitorState, 0.5, WarnTypes.FREQUENT_RESTORING.getDesc())) {
            RestorationInfo restorationInfo = new RestorationInfo();
            restorationInfo.setRestoredTimes(currentCheckpointCounts.getRestored());

            // 获取最近一次拉起的信息
            CheckpointDetail latestRestoredCheckpoint = checkpointInfo.getLatestCheckpointInfo().getLatestRestoredCheckpoint();
            if (latestRestoredCheckpoint != null) {
                restorationInfo.setLatestRestoredTimestamp(latestRestoredCheckpoint.getRestoreTimestamp());
                restorationInfo.setIsSavepoint(latestRestoredCheckpoint.getIsSavepoint());
            }

            warnInfos.add(restorationInfo);
        }

        //CK未触发告警
        long unTriggerTs = System.currentTimeMillis() - triggerTimestampLatest;
        if (unTriggerTs > HOUR_MS && frequencyCheck(taskMonitorState, 1, WarnTypes.CK_UN_TRIGGER.getDesc())) {
            CkUnTriggerInfo ckUnTriggerInfo = new CkUnTriggerInfo();
            ckUnTriggerInfo.setTriggerHour(unTriggerTs / HOUR_MS);
            ckUnTriggerInfo.setTriggerTimestamp(triggerTimestampLatest);
            warnInfos.add(ckUnTriggerInfo);
        }

        return warnInfos;
    }

    /**
     * 避免频繁告警
     *
     * @param taskMonitorState 状态
     * @param hours            告警频率 h
     * @param mark             告警唯一标识
     * @return 是否满足频率要求
     */
    private boolean frequencyCheck(TaskMonitorState taskMonitorState, double hours, String mark) {
        Map<Integer, Date> errMsgCache = taskMonitorState.getErrMsgCache();

        if (StringUtils.isBlank(mark)) return false;
        int msgHashCode = mark.hashCode();
        Date date = errMsgCache.get(msgHashCode);

        if (date != null && (System.currentTimeMillis() - date.getTime()) > 7 * 24 * 60 * 60 * 1000) {
            errMsgCache.remove(msgHashCode);
        }

        //出现过一次就不再发送
        if (hours < 0 && date != null) return false;

        if (date == null || (System.currentTimeMillis() - date.getTime()) > hours * 60 * 60 * 1000) {
            errMsgCache.put(msgHashCode, new Date());
            return true;
        } else {
            log.info("超过频率,不发送:{}|{}", taskMonitorState.getTaskId(), mark);
            return false;
        }
    }

    private int getContinueFailCnt(List<CheckpointDetail> history) {
        if (history == null || history.size() <= 1) return 0;

        int cnt = 0;
        for (int i = history.size() - 1; i >= 0; i--) {
            if ("FAILED".equals(history.get(i).getStatus())) cnt++;
            else break;
        }

        return cnt;
    }

    private List<WarnInfo> checkGC(FlinkTaskInfoDto taskInfo, TaskMonitorState taskMonitorState) {
        // 上次检查时间不足阈值，暂时不检查
        long gcCheckedTimestamp = taskMonitorState.getGcCheckedTimestamp();
        long currentTimeMillis = System.currentTimeMillis();
        if (TimeUnit.MILLISECONDS.toMinutes(currentTimeMillis - gcCheckedTimestamp) < monitorConfigurations.getGcCheckIntervalMinutes()) {
            return Collections.emptyList();
        }

        // 更新GC检查时间
        taskMonitorState.setGcCheckedTimestamp(currentTimeMillis);

        List<WarnInfo> warnInfos = new ArrayList<>();

        // 作业运行时间增量（用于计算GC频率）
        long deltaRunningDuration = taskInfo.getCurrentRunningDuration() - taskMonitorState.getCurrentRunningDuration();

        // 状态上一次GC信息
        Map<String, GarbageCollectDto> stateFullGcMap = Optional.ofNullable(taskMonitorState.getFullGc()).orElse(new HashMap<>(0));

        boolean gcExceptionRecorded = false;
        String flinkDashboardUrlPrefix = taskInfo.getFlinkDashboardUrlPrefix();
        List<JSONObject> taskManagerList = FlinkDashboardUtil.getTaskManagerList(flinkDashboardUrlPrefix);
        Map<String, GarbageCollectDto> currentFullGc = new HashMap<>(taskManagerList.size());

        // 遍历JM容器的获取各TM的GC指标
        for (JSONObject taskManagerInfo : taskManagerList) {
            String containerId = taskManagerInfo.getString("id");
            TaskManagerMetricsInfoDto containerInfo = FlinkDashboardUtil.getTaskManagerInfo(flinkDashboardUrlPrefix, containerId);
            if (containerInfo == null) {
                continue;
            }

            if (gcExceptionRecorded) {
                break;
            }

            MemoryMetricsDto metrics = containerInfo.getMetrics();
            for (GarbageCollectDto garbageCollector : metrics.getGarbageCollectors()) {
                // 只看FullGC指标
                if (!StringUtils.equals("PS_MarkSweep", garbageCollector.getName())) {
                    continue;
                }

                GarbageCollectDto prevFullGc = stateFullGcMap.computeIfAbsent(containerId, k -> new GarbageCollectDto());

                // 更新GC状态
                currentFullGc.put(containerId, garbageCollector);

                // GC异常记录过了就不在记录
                if (gcExceptionRecorded) {
                    continue;
                }

                // GC次数和时间增量（用于计算平均GC时间）
                long deltaFullGcCount = garbageCollector.getCount() - prevFullGc.getCount();
                long deltaFullGcTime = garbageCollector.getTime() - prevFullGc.getTime();

                if (isSlowFullGC(deltaFullGcCount, deltaFullGcTime)) {
                    // 慢GC信息
                    SlowGcInfo slowGcInfo = new SlowGcInfo();
                    slowGcInfo.setContainerId(containerId);
                    slowGcInfo.setDeltaFullGcCount(deltaFullGcCount);
                    slowGcInfo.setDeltaGcTimeMs(deltaFullGcTime);
                    warnInfos.add(slowGcInfo);
                    gcExceptionRecorded = true;
                }

                // 频繁GC信息
                if (isFrequentFullGC(deltaFullGcCount, deltaRunningDuration)) {
                    FrequentFullGcInfo frequentFullGcInfo = new FrequentFullGcInfo();
                    frequentFullGcInfo.setContainerId(containerId);
                    frequentFullGcInfo.setDeltaFullGcCount(deltaFullGcCount);
                    frequentFullGcInfo.setDeltaRunningDurationMs(deltaRunningDuration);
                    frequentFullGcInfo.setTotalFullGcCount(garbageCollector.getCount());
                    frequentFullGcInfo.setTotalFullGcTimeMs(garbageCollector.getTime());
                    warnInfos.add(frequentFullGcInfo);
                    gcExceptionRecorded = true;
                }
            }
        }

        // 更新状态的GC信息
        taskMonitorState.setFullGc(currentFullGc);

        return warnInfos;
    }


    /**
     * 判断是否为慢GC
     *
     * @param deltaFullGcCount 检查间隔期间增量GC次数
     * @param deltaGcTimeMs    检查间隔期间增量GC时间（毫秒）
     */
    private boolean isSlowFullGC(long deltaFullGcCount, long deltaGcTimeMs) {
        if (deltaGcTimeMs == 0) {
            return false;
        }

        if (deltaFullGcCount < 2L) {
            return false;
        }

        int avgGcTimeSeconds = Math.round(deltaGcTimeMs / 1000.0F / deltaFullGcCount);
        return avgGcTimeSeconds > monitorConfigurations.getSlowGcThresholdSeconds();
    }

    /**
     * 判断是否为频繁GC
     *
     * @param deltaFullGcCount       检查间隔期间增量GC次数
     * @param deltaRunningDurationMs 检查间隔期间增量任务运行时间（毫秒）
     */
    private boolean isFrequentFullGC(long deltaFullGcCount, long deltaRunningDurationMs) {
        if (deltaRunningDurationMs == 0) {
            return false;
        }

        if (deltaFullGcCount < 3) {
            return false;
        }

        float gcTimeInHour = deltaRunningDurationMs / (1000.0F * 3600);
        return Math.round(deltaFullGcCount / gcTimeInHour) > monitorConfigurations.getFrequentGcThresholdPerHour();
    }

    private boolean isSlowCheckpoint(long endToEndDuration) {
        return endToEndDuration > monitorConfigurations.getSlowCheckpointThresholdMs();
    }

    private WarnInfo checkTaskManagerExceptions(FlinkTaskInfoDto taskInfo, TaskMonitorState taskMonitorState) throws Exception {
        ExceptionDto exceptionDto = FlinkDashboardUtil.getExceptions(taskInfo.getFlinkDashboardUrlPrefix(), taskInfo.getJobId());
        if (exceptionDto == null) {
            return null;
        }

        String rootException = exceptionDto.getRootException();
        if (StringUtils.isBlank(rootException) || isErrIgnore(rootException)) {
            // 任务没有异常直接返回
            return null;
        }


        // 忽略3天以上的异常
        Long exceptionTimestamp = exceptionDto.getTimestamp();
        if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - exceptionTimestamp) >= 3L) {
            return null;
        }

        // 异常时间戳没有变化，不处理
        long prevExceptionTimestamp = taskMonitorState.getTaskManagerExceptionTimestamp();
        if (exceptionTimestamp == prevExceptionTimestamp) {
            return null;
        }

        // 更新异常时间戳
        taskMonitorState.setTaskManagerExceptionTimestamp(exceptionTimestamp);

        // TM重启异常单独处理
        Matcher matcher = TM_LOST_EXCEPTION_PATTERN.matcher(rootException);
        if (matcher.find()) {
            String taskManager = matcher.group(1);
            TaskManagerLostInfo taskManagerLostInfo = new TaskManagerLostInfo();
            taskManagerLostInfo.setTaskManager(taskManager);
            return taskManagerLostInfo;
        }

        // 截取异常信息
        String exceptionDatetime = DateUtil.long2DateStr(exceptionTimestamp);
        StringBuilder logInfoBuilder = new StringBuilder("[")
                .append(exceptionDatetime)
                .append("]");

        // 解析异常堆栈
        String[] exceptionTrace = rootException.split("\n");

        // 导致任务的直接异常，即异常堆栈位于最顶部的异常
        String directException = exceptionTrace[0];
        logInfoBuilder.append(directException);

        // 向下搜索，检查是否有其他导致栈顶异常的原因
        for (int i = 1; i < exceptionTrace.length; i++) {
            String trace = exceptionTrace[i];
            if (trace.contains("Caused by")) {
                logInfoBuilder.append("\n").append(trace);
                break;
            }
        }

        ExceptionInfo exceptionInfo = new ExceptionInfo();
        exceptionInfo.setTimestamp(exceptionDto.getTimestamp());
        exceptionInfo.setLogInfo(logInfoBuilder.toString());
        exceptionInfo.setDirectException(directException);
        return exceptionInfo;
    }

    private static boolean isErrIgnore(String msg) {
        for (String err : BDPConst.IGNORE_ERRS) if (msg.contains(err)) return true;
        return false;
    }

    private WarnInfo checkPhysicalMemoryUsage(FlinkTaskInfoDto taskInfo, TaskMonitorState taskMonitorState) {
        long lastWarnTimestamp = taskMonitorState.getPhysicalMemoryReachCriticalLevelTimestamp();
        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastWarnTimestamp) < monitorConfigurations.getPhysicalMemoryWarnInterval()) {
            return null;
        }

        long taskManagerMaxMemoryMB = taskInfo.getTmMem();

        Integer numberOfTaskManagers = taskInfo.getTmNum();
        if (numberOfTaskManagers == null) {
            log.warn("任务的TM为空，任务数据：{}", taskInfo);
            return null;
        }

        String flinkDashboardUrl = taskInfo.getFlinkDashboardUrlPrefix();
        String jobId = taskInfo.getJobId();
        TaskGraphDto taskGraph = FlinkDashboardUtil.getTaskGraph(flinkDashboardUrl, jobId);
        List<VertexDto> vertices = taskGraph.getVertices();

        String physicalMemoryMonitorOperatorId = null;
        for (VertexDto vertex : vertices) {
            if (!vertex.getName().contains("FlinkAuxSource")) {
                continue;
            }

            // 算子中包含ProcessStatusMonitorSource说明任务已经开启了物理内存监控
            physicalMemoryMonitorOperatorId = vertex.getId();
            break;
        }

        // 找不到物理内存监控的算子ID默认没有开启监控
        if (physicalMemoryMonitorOperatorId == null) {
            return null;
        }


        SubTaskMainDto subTaskInfo = FlinkDashboardUtil.getSubTaskInfo(flinkDashboardUrl, jobId, physicalMemoryMonitorOperatorId);
        if (subTaskInfo == null) {
            return null;
        }
        List<SubtaskDto> subtasks = subTaskInfo.getSubtasks();
        if (CollectionUtils.isEmpty(subtasks)) {
            return null;
        }

        Map<String, String> taskManagerMertricNameMap = new HashMap<>(numberOfTaskManagers);
        for (SubtaskDto subtask : subtasks) {
            String taskManagerId = subtask.getTaskManagerId();
            if (taskManagerMertricNameMap.containsKey(taskManagerId)) {
                continue;
            }
            int subtaskIndex = subtask.getSubtaskIndex();
            String metricName = subtaskIndex + ".Source__FlinkAuxSource.PhysicalMemoryUsage";

            taskManagerMertricNameMap.put(taskManagerId, metricName);
        }

        List<String> metricsNames = new ArrayList<>(taskManagerMertricNameMap.values());
        String response = FlinkDashboardUtil.getSubtaskMetrics(flinkDashboardUrl, jobId, physicalMemoryMonitorOperatorId, metricsNames);
        List<Metric<Long>> metricList = JSON.parseObject(response, new TypeReference<List<Metric<Long>>>() {
        }.getType());
        if (CollectionUtils.isEmpty(metricList)) {
            return null;
        }

        Map<String, Long> metricValueMap = new HashMap<>(metricList.size());
        for (Metric<Long> metric : metricList) {
            metricValueMap.put(metric.getId(), metric.getValue());
        }

        for (Map.Entry<String, String> entry : taskManagerMertricNameMap.entrySet()) {
            String taskManagerId = entry.getKey();
            String metricName = entry.getValue();
            Long physicalMemoryUsageBytes = metricValueMap.get(metricName);
            if (physicalMemoryUsageBytes == null) {
                continue;
            }
            long physicalMemoryUsageMB = (physicalMemoryUsageBytes >> 20);

            long remainingPhysicalMemoryMB = taskManagerMaxMemoryMB - physicalMemoryUsageMB;
            if (remainingPhysicalMemoryMB < monitorConfigurations.getPhysicalMemoryMBWarnThreshold()) {
                PhysicalMemoryReachCriticalLevelInfo physicalMemoryReachCriticalLevelInfo = new PhysicalMemoryReachCriticalLevelInfo();
                physicalMemoryReachCriticalLevelInfo.setTotalMemoryMB(taskManagerMaxMemoryMB);
                physicalMemoryReachCriticalLevelInfo.setContainerId(taskManagerId);
                physicalMemoryReachCriticalLevelInfo.setMemoryUsedMB(physicalMemoryUsageMB);
                taskMonitorState.setPhysicalMemoryReachCriticalLevelTimestamp(System.currentTimeMillis());
                return physicalMemoryReachCriticalLevelInfo;
            }
        }

        return null;
    }
}
