package com.demo.model;

import lombok.Data;

/**
 * 进程状态DTO
 *
 * @author sky
 */
@Data
public class ProcessStatusDto {

    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 进程ID
     */
    private Long pid;
    /**
     * 进程优先级
     */
    private Integer priority;
    /**
     * 进程nice值
     */
    private Integer nice;
    /**
     * CPU使用率（%）
     */
    private Double cpuUsePercentage;
    /**
     * 内存使用率（%）
     */
    private Double memoryUsePercentage;
    /**
     * VIRT-虚拟内存占用大小（字节）
     */
    private Long virtualMemorySize;
    /**
     * RES-物理内存占用大小（字节）
     */
    private Long residentMemorySize;
    /**
     * 时间戳
     */
    private Long timestamp;

}
