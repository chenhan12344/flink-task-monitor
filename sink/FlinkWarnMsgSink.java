package com.demo.sink;

import com.demo.utils.ParameterToolFetcher;
import com.google.common.collect.ImmutableList;
import com.demo.model.bdp.FlinkWarnMsgDto;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.util.List;

/**
 * @author sky
 */
public class FlinkWarnMsgSink extends RichSinkFunction<FlinkWarnMsgDto> {

    /**
     * 告警推送消息
     */
    private List<String> notifyFusionGroupIds;

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool parameterTool = ParameterToolFetcher.fetch(this);
        this.notifyFusionGroupIds = ImmutableList.copyOf(parameterTool.get("notifyFusionGroupIds").split(","));
    }

    @Override
    public void invoke(FlinkWarnMsgDto errMsgDto, Context context) throws Exception {
    }

}
