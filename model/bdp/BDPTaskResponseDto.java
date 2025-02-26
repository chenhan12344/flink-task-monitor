package com.demo.model.bdp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:00
 */
@Data
public class BDPTaskResponseDto implements Serializable {

    private static final long serialVersionUID = 7945420739578035938L;

    /**
     * 任务基本信息
     */
    private TaskDto task;
    /**
     * 任务运行信息
     */
    private TaskRunningDto taskRunning;

}