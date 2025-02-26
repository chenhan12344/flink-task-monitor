package com.demo.model.flinkdashboard.checkpoint;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author sky
 */
@Data
public class CheckpointInfo {

    @JSONField(name = "counts")
    private CheckpointCounts checkpointCounts;

    @JSONField(name = "history")
    private List<CheckpointDetail> checkpointHistory;

    @JSONField(name = "latest")
    private LatestCheckpointInfo latestCheckpointInfo;

}
