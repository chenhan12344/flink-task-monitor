package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class VertexDto implements Serializable {

    private static final long serialVersionUID = 2879012729956260330L;

    private String id;
    private String name;
    private String status;
    @JSONField(name = "start-time")
    private long startTime;
    @JSONField(name = "end-time")
    private long endTime;
    private int parallelism;
    private TaskMetricsDto metrics;
}

