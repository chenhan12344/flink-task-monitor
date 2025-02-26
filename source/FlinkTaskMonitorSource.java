package com.demo.source;

import com.demo.utils.ParameterToolFetcher;
import com.google.common.collect.ImmutableList;
import com.demo.utils.BDPUtil;
import com.demo.model.bdp.BDPParamDto;
import com.demo.model.bdp.BDPTaskResponseDto;
import com.demo.model.bdp.TaskDto;
import com.demo.model.bdp.TaskRunningDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sky
 */
@Slf4j
public class FlinkTaskMonitorSource extends RichParallelSourceFunction<BDPTaskResponseDto> {

    private List<String> monitorTaskOwners;
    private String bdpCookie;
    private long monitorIntervalMinutes;
    private boolean keepRunning = true;
    private long monitorLoop;

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool parameterTool = ParameterToolFetcher.fetch(this);
        this.monitorTaskOwners = ImmutableList.copyOf(parameterTool.get("monitorTaskOwners").split(","));
        this.bdpCookie = "BDPSESSION=" + System.getenv("BDPSESSION");
        this.monitorIntervalMinutes = parameterTool.getLong("monitorInterval", 1L);
    }

    @Override
    public void run(SourceContext<BDPTaskResponseDto> ctx) throws Exception {

        int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
        int parallelism = getRuntimeContext().getNumberOfParallelSubtasks();

        while (this.keepRunning) {
            monitorLoop++;
            log.info("开始第{}次检查", monitorLoop);
            for (String owner : monitorTaskOwners) {

                if (Math.abs(owner.hashCode()) % parallelism != indexOfThisSubtask) {
                    continue;
                }

                try {
                    List<BDPTaskResponseDto> taskList = BDPUtil.getTaskList(new BDPParamDto(owner), bdpCookie);
                    for (BDPTaskResponseDto bdpTaskResponseDto : taskList) {
                        ctx.collect(bdpTaskResponseDto);
                    }
                } catch (Exception e) {
                    // Cookie过期给一个空的任务信息下去
                    BDPTaskResponseDto cookieExpiredDto = new BDPTaskResponseDto();
                    cookieExpiredDto.setTask(new TaskDto());
                    cookieExpiredDto.setTaskRunning(new TaskRunningDto());
                    ctx.collect(cookieExpiredDto);
                    // 一小时发一次避免皮频繁发送消息
                    ctx.markAsTemporarilyIdle();
                    TimeUnit.HOURS.sleep(1L);
                }

            }
            ctx.markAsTemporarilyIdle();
            TimeUnit.MINUTES.sleep(monitorIntervalMinutes);
        }
    }

    @Override
    public void cancel() {
        this.keepRunning = false;
    }
}
