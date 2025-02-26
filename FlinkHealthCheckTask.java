package com.demo;

import com.demo.map.FlinkTaskInfoMap;
import com.demo.model.TaskEnv;
import com.demo.model.bdp.FlinkWarnMsgDto;
import com.demo.model.monitor.FlinkTaskInfoDto;
import com.demo.process.FlinkTaskHealthCheckProcess;
import com.demo.sink.FlinkWarnMsgSink;
import com.demo.source.FlinkTaskMonitorSource;
import com.demo.utils.FlinkTaskUtil;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @Description Flink健康监控
 * @Author 
 * @CreateDate 2023/4/12 22:23
 */
public class FlinkHealthCheckTask {

    public static void main(String[] args) throws Exception {

        TaskEnv taskEnv = FlinkTaskUtil.configureAndValidateEnv(args);
        StreamExecutionEnvironment env = taskEnv.getExecutionEnvironment();

        SingleOutputStreamOperator<FlinkWarnMsgDto> warnMsgStream = env.addSource(new FlinkTaskMonitorSource())
                .name("FlinkTaskMonitorSource")
                .uid("FlinkTaskMonitorSource")
                .map(new FlinkTaskInfoMap())
                .name("FlinkTaskInfoMap")
                .uid("FlinkTaskInfoMap")
                .keyBy(FlinkTaskInfoDto::getTaskId)
                .process(new FlinkTaskHealthCheckProcess())
                .name("FlinkTaskMonitorProcess")
                .uid("FlinkTaskMonitorProcess");
        if (taskEnv.isDev()) {
            warnMsgStream.addSink(new SinkFunction<FlinkWarnMsgDto>() {
                @Override
                public void invoke(FlinkWarnMsgDto value) throws Exception {
                    System.out.println(value.buildMsg());
                }
            });
        } else {
            warnMsgStream.addSink(new FlinkWarnMsgSink())
                    .name("FlinkWarnFusionMsgSink")
                    .uid("FlinkWarnFusionMsgSink");
        }

        env.execute(taskEnv.getJobName());
    }
}
