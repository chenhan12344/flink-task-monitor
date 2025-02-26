package com.demo.map;

import com.demo.model.bdp.BDPTaskResponseDto;
import com.demo.model.bdp.TaskDto;
import com.demo.model.bdp.TaskRunningDto;
import com.demo.model.monitor.FlinkTaskInfoDto;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author sky
 */
public class FlinkTaskInfoMap implements MapFunction<BDPTaskResponseDto, FlinkTaskInfoDto> {

    @Override
    public FlinkTaskInfoDto map(BDPTaskResponseDto bdpTaskResponseDto) throws Exception {
        FlinkTaskInfoDto flinkTaskInfoDto = new FlinkTaskInfoDto();

        // 任务基本信息
        TaskDto task = bdpTaskResponseDto.getTask();
        flinkTaskInfoDto.setAppId(task.getAppId());
        flinkTaskInfoDto.setAppName(task.getAppName());
        flinkTaskInfoDto.setTaskId(task.getTaskId());
        flinkTaskInfoDto.setTaskName(task.getName());
        flinkTaskInfoDto.setTaskDescription(task.getDescription());
        flinkTaskInfoDto.setOwnerId(task.getOwner());
        flinkTaskInfoDto.setOwnerName(task.getOwnerName());
        flinkTaskInfoDto.setSystemCode(task.getSystemCode());

        // 任务运行信息
        TaskRunningDto taskRunning = bdpTaskResponseDto.getTaskRunning();
        flinkTaskInfoDto.setClusterName(taskRunning.getClusterName());
        flinkTaskInfoDto.setLaunchTime(taskRunning.getLaunchTime());
        flinkTaskInfoDto.setFlinkDashboardUrlPrefix(taskRunning.getTaskUrl());
        flinkTaskInfoDto.setJobId(taskRunning.getJobId());
        flinkTaskInfoDto.setTmMem(taskRunning.getTmMem());
        flinkTaskInfoDto.setTmNum(taskRunning.getTmNum());


        return flinkTaskInfoDto;
    }
}
