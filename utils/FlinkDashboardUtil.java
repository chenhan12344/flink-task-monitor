package com.demo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.demo.model.bdp.JobDto;
import com.demo.model.flinkdashboard.*;
import com.demo.model.flinkdashboard.checkpoint.CheckpointInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description FlinkDashboard信息获取接口
 * @Author
 * @Date 2023/10/24 15:11
 */

public class FlinkDashboardUtil {
    /**
     * 获取 TaskManager 信息集合
     *
     * @return List<TaskManagerInfo>
     */
    public static List<JSONObject> getTaskManagerList(String clusterUrlPrefix) {
        JSONObject taskManagersResponseDto = HttpUtilNew.doGet(clusterUrlPrefix + "/taskmanagers", JSONObject.class);
        if (taskManagersResponseDto == null) return new ArrayList<>();
        String taskmanagers = taskManagersResponseDto.getString("taskmanagers");
        return JSON.parseArray(taskmanagers, JSONObject.class);
    }

    /**
     * 获取 单个Container 信息集合
     *
     * @param containerId containerId
     * @return TaskManagerMetricsInfo
     */
    public static TaskManagerMetricsInfoDto getTaskManagerInfo(String clusterUrlPrefix, String containerId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "/taskmanagers/" + containerId, TaskManagerMetricsInfoDto.class);
    }

    /**
     * 获取 任务异常信息
     *
     * @param clusterUrlPrefix clusterUrlPrefix
     * @return JSONObject
     */
    public static ExceptionDto getExceptions(String clusterUrlPrefix, String jobId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "jobs/" + jobId + "/exceptions?maxExceptions=1", ExceptionDto.class);
    }

    /**
     * 获取 任务列表(通常仅有一个任务)
     *
     * @param clusterUrlPrefix clusterUrlPrefix
     * @return return
     */
    public static JobDto getJobsOverview(String clusterUrlPrefix) {
        JSONObject response = HttpUtilNew.doGet(clusterUrlPrefix + "/jobs/overview", JSONObject.class);
        if (response == null) return null;
        String jobs = response.getString("jobs");
        return JSON.parseArray(jobs, JobDto.class).get(0);
    }

    public static JobOverviewDto getJobOverview(String clusterUrlPrefix, String jobId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "/jobs/" + jobId, JobOverviewDto.class);
    }

    /**
     * 获取 ck信息集合
     *
     * @param clusterUrlPrefix clusterUrlPrefix
     * @param jobId            jobId
     * @return CheckpointingStatistics
     */
    public static CheckpointInfo getCheckpoints(String clusterUrlPrefix, String jobId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "/jobs/" + jobId + "/checkpoints", CheckpointInfo.class);
    }

    /**
     * 获取任务基本信息(算子列表)
     *
     * @param clusterUrlPrefix clusterUrlPrefix
     * @param jobId            jobId
     * @return TaskGraphDto
     */
    public static TaskGraphDto getTaskGraph(String clusterUrlPrefix, String jobId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "/jobs/" + jobId, TaskGraphDto.class);
    }

    /**
     * 获取算子压力信息 backPressuredTimeMsPerSecond,busyTimeMsPerSecond
     *
     * @param clusterUrlPrefix clusterUrlPrefix
     * @param jobId            jobId
     * @param slotId           slotId
     * @return MathIndexDto
     */
    public static List<MathIndexDto> getSlotPressure(String clusterUrlPrefix, String jobId, String slotId) {
        String str = HttpUtilNew.doGet(clusterUrlPrefix + "/jobs/" + jobId + "/vertices/" + slotId + "/subtasks/metrics?get=backPressuredTimeMsPerSecond,busyTimeMsPerSecond", String.class);
        return JSON.parseArray(str, MathIndexDto.class);
    }

    public static SubTaskMainDto getSubTaskInfo(String clusterUrlPrefix, String jobId, String slotId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "/jobs/" + jobId + "/vertices/" + slotId, SubTaskMainDto.class);
    }

    public static LogInfo getJobManagerLogs(String clusterUrlPrefix) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "jobmanager/logs", LogInfo.class);
    }

    /**
     * 获取JM日志
     *
     * @param clusterUrlPrefix clusterUrlPrefix
     * @param jobId            jobId
     * @return String
     */
    public static String getJobManagerLog(String clusterUrlPrefix, String jobId) {
        return HttpUtilNew.doGet(clusterUrlPrefix + "jobmanager/log");
    }

    public static String getSubtaskMetrics(String clusterUrlPrefix, String jobId, String vertexId, List<String> metricNames) {
        String url = clusterUrlPrefix + "/jobs/" + jobId + "/vertices/" + vertexId + "/metrics?get=" + String.join(",", metricNames);
        return HttpUtilNew.doGet(url);
    }
}
