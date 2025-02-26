package com.demo.source;

import com.demo.metrics.PhysicalMemoryGauge;
import com.demo.model.ProcessStatusDto;
import com.demo.utils.FlinkTaskUtil;
import com.demo.utils.OsUtil;
import com.demo.utils.ParameterToolFetcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.util.concurrent.TimeUnit;

/**
 * @author sky
 */
@Slf4j
public class FlinkAuxSource extends RichParallelSourceFunction<ProcessStatusDto> {

    public static final String CONFIG_KEY_PS_MONITOR_INTERVAL = "psMonitorInterval";
    public static final String CONFIG_KEY_PS_MONITOR_INTERVAL_UNIT = "psMonitorIntervalUnit";


    private long monitorInterval;
    private TimeUnit intervalUnit;
    private boolean keepRunning = true;
    private boolean processMonitoringEnabled;
    private transient PhysicalMemoryGauge physicalMemoryGauge;

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool parameterTool = ParameterToolFetcher.fetch(this);
        monitorInterval = parameterTool.getLong(CONFIG_KEY_PS_MONITOR_INTERVAL, 30L);
        intervalUnit = TimeUnit.valueOf(parameterTool.get(CONFIG_KEY_PS_MONITOR_INTERVAL_UNIT, "SECONDS"));
        processMonitoringEnabled = parameterTool.getBoolean(FlinkTaskUtil.CONFIG_KEY_ENABLE_PROCESS_MONITORING, true);

        this.physicalMemoryGauge = new PhysicalMemoryGauge();
        getRuntimeContext().getMetricGroup()
                .gauge("PhysicalMemoryUsage", physicalMemoryGauge);
    }


    @Override
    public void run(SourceContext<ProcessStatusDto> ctx) throws Exception {
        while (keepRunning) {
            try {
                if (processMonitoringEnabled) {
                    ProcessStatusDto currentProcessStatus = OsUtil.getCurrentProcessStatus();
                    physicalMemoryGauge.update(currentProcessStatus.getResidentMemorySize());
                    ctx.collect(currentProcessStatus);
                }
            } catch (Exception e) {
                log.warn("获取进程信息失败", e);
                continue;
            }
            ctx.markAsTemporarilyIdle();
            intervalUnit.sleep(monitorInterval);
        }
    }

    @Override
    public void cancel() {
        keepRunning = false;
    }
}
