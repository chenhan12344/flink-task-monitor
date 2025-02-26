package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 子任务负载
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class SubtaskBackpressureDto implements Serializable {

    private static final long serialVersionUID = 6393971550408245017L;

    private int subtask;
    @JSONField(name = "backpressure-level")
    private String backpressureLevel;
    private Double ratio;
    private Double idleRatio;
    private Double busyRatio;
}

