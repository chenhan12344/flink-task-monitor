package com.demo.model.flinkdashboard;

import lombok.Data;
import org.apache.flink.runtime.rest.messages.taskmanager.TaskManagerInfo;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * @Author 
 * @Date 2023/10/23 16:02
 */
@Data
public class TaskManagersResponseDto implements Serializable {
    private static final long serialVersionUID = 9034283144541656059L;
    private List<TaskManagerInfo> taskmanagers;
}
