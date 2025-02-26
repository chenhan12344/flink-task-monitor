package com.demo.model.flinkdashboard;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/23 16:02
 */
@Data
public class TaskManagerMetricsInfoDto implements Serializable {
    private static final long serialVersionUID = 9034283144541656059L;
    private String id;
    private MemoryMetricsDto metrics;
}
