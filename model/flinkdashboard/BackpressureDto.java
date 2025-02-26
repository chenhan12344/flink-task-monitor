package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class BackpressureDto implements Serializable {

    private static final long serialVersionUID = 2879012729956260330L;

    private String status; //ok
    @JSONField(name = "backpressure-level")
    private String backpressureLevel;
    private List<SubtaskBackpressureDto> subtasks;
}

