package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.apache.flink.runtime.rest.messages.checkpoints.CheckpointStatistics;
import org.apache.flink.runtime.rest.messages.checkpoints.CheckpointingStatistics;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @Description
 * @Author 
 * @Date 2023/10/25 17:25
 */

public class CheckpointingStatisticsDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONObject jsonObject = parser.parseObject();
        CheckpointingStatistics.Counts counts = jsonObject.getObject("counts", CheckpointingStatistics.Counts.class);
        CheckpointingStatistics.Summary summary = jsonObject.getObject("summary", CheckpointingStatistics.Summary.class);
        CheckpointingStatistics.LatestCheckpoints latest = jsonObject.getObject("latest", CheckpointingStatistics.LatestCheckpoints.class);
        List<CheckpointStatistics> history = JSONObject.parseArray(jsonObject.getString("history"), CheckpointStatistics.class);
        CheckpointingStatistics checkpointingStatistics = new CheckpointingStatistics(counts, summary, latest, history);
        return (T) checkpointingStatistics;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}