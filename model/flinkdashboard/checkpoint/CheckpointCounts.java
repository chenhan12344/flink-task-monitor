package com.demo.model.flinkdashboard.checkpoint;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Flink Checkpoint统计数据
 *
 * @author sky
 */
@Data
public class CheckpointCounts {

    private int completed;

    private int failed;

    @JSONField(name = "in_progress")
    private int inProgress;

    private int restored;

    private int total;

}
