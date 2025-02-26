package com.demo.model.flinkdashboard.checkpoint;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author sky
 */
@Data
public class LatestCheckpointInfo {

    /**
     * 最近一次成功的Checkpoint
     */
    @JSONField(name = "completed")
    private CheckpointDetail latestCompletedCheckpoint;

    /**
     * 最近一次失败的Checkpoint
     */
    @JSONField(name = "failed")
    private CheckpointDetail latestFailedCheckpoint;

    /**
     * 最近一次恢复的Checkpoint
     */
    @JSONField(name = "restored")
    private CheckpointDetail latestRestoredCheckpoint;

    /**
     * 最近一次成功的Savepoint
     */
    @JSONField(name = "savepoint")
    private CheckpointDetail latestSavepoint;


}
