package com.demo.model.flinkdashboard;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 15:31
 * {
 * "jid": "eafafb6adbdba810addc39430c7e1fcd",
 * "name": "尾货汇总线路配载均分_上卷粗粒度汇总_prod",
 * "nodes": [{
 * "id": "20d5520332f1f8d11568a1b00d764e0b",
 * "parallelism": 32,
 * "operator": "",
 * "operator_strategy": "",
 * "description": "Sink: flowCargoSummaryDetailExtSink",
 * "inputs": [{
 * "num": 0,
 * "id": "e2e5c52f614dfb819953137b734e65fd",
 * "ship_strategy": "HASH",
 * "exchange": "pipelined_bounded"
 * }],
 * "optimizer_properties": {}        * 		} ]
 * }
 */
@Data
public class TaskPlanDto implements Serializable {

    private static final long serialVersionUID = 5500370994642794853L;

    private String jid;
    private String name;
    private List<TaskNodeDto> nodes;
}

