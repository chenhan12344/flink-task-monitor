package com.demo.model.flinkdashboard.checkpoint;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author sky
 */
@Data
public class CheckpointDetail {

    /**
     * CheckpointId
     */
    @JSONField(name = "id")
    private Long id;

    /**
     * Checkpoint状态
     */
    @JSONField(name = "status")
    private String status;

    /**
     * 是否是Savepoint
     */
    @JSONField(name = "is_savepoint")
    private Boolean isSavepoint;

    /**
     * Checkpoint触发时间戳
     */
    @JSONField(name = "trigger_timestamp")
    private Long triggerTimestamp;

    /**
     * 算子响应Checkpoint最新时间戳
     */
    @JSONField(name = "latest_ack_timestamp")
    private Long latestAckTimestamp;

    /**
     * 状态大小（Byte）
     */
    @JSONField(name = "state_size")
    private Long stateSize;

    /**
     * 端到端延时
     */
    @JSONField(name = "end_to_end_duration")
    private Long endToEndDuration;

    /**
     * 对齐时缓冲大小
     */
    @JSONField(name = "alignment_buffered")
    private Long alignmentBuffered;

    /**
     *
     */
    @JSONField(name = "processed_data")
    private Long processedData;

    /**
     *
     */
    @JSONField(name = "persisted_data")
    private Long persistedData;

    /**
     *
     */
    @JSONField(name = "num_subtasks")
    private Integer numSubtasks;

    /**
     * 已响应Checkpoint的算子子任务数量
     */
    @JSONField(name = "num_acknowledged_subtasks")
    private Integer numAcknowledgedSubtasks;

    /**
     * Checkpoint类型
     */
    @JSONField(name = "checkpoint_type")
    private String checkpointType;

    /**
     * Checkpoint外部存储位置
     */
    @JSONField(name = "external_path")
    private String externalPath;

    /**
     * Checkpoint是否被丢弃
     */
    @JSONField(name = "discarded")
    private boolean discarded;

    /**
     * Checkpoint拉起时间戳
     */
    @JSONField(name = "restore_timestamp")
    private Long restoreTimestamp;

}
