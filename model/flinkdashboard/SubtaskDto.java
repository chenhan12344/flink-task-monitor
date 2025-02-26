package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 子任务实体
 * @Author 
 * @Date 2023/10/24 15:31
 * {
 * "subtask": 0,
 * "status": "RUNNING",
 * "attempt": 0,
 * "host": "CNSZ20PL6274",
 * "start-time": 1704856520131,
 * "end-time": -1,
 * "duration": 27069814,
 * "metrics": {
 * "read-bytes": 5533704605,
 * "read-bytes-complete": true,
 * "write-bytes": 1783974478,
 * "write-bytes-complete": true,
 * "read-records": 9473528,
 * "read-records-complete": true,
 * "write-records": 3361963,
 * "write-records-complete": true
 * },
 * "taskmanager-id": "container_e13_1691118368136_8359_01_000029",
 * "start_time": 1704856520131
 * }
 */
@Data
public class SubtaskDto implements Serializable {

    private static final long serialVersionUID = 6393971550408245017L;

    /**
     * 算子子任务索引
     */
    @JSONField(name = "subtask")
    private Integer subtaskIndex;

    private String host;

    /**
     * 算子状态
     */
    private String status;

    /**
     * 算子所在TaskManager
     */
    @JSONField(name = "taskmanager-id")
    private String taskManagerId;

    /**
     * 算子监控指标
     */
    private TaskMetricsDto metrics;
}

