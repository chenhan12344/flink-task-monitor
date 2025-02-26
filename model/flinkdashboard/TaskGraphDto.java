package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 任务执行计划信息
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class TaskGraphDto implements Serializable {

    private static final long serialVersionUID = 6393971550408245017L;

    private String jid;
    private String name;
    private Boolean isStoppable;
    private String state;
    @JSONField(name = "start-time")
    private long startTime;
    @JSONField(name = "end-time")
    private long endTime;
    private long duration;
    private List<VertexDto> vertices;
    private TaskPlanDto plan;
}

