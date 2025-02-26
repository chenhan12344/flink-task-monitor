package com.demo.model.flinkdashboard;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 子任务实体
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class SubTaskMainDto implements Serializable {

    private static final long serialVersionUID = 6393971550408245017L;

    private String id;
    private String name;
    private int parallelism;
    private List<SubtaskDto> subtasks;
}

