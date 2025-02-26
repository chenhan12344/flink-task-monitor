package com.demo.model.flinkdashboard;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 内存GC指标
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class MemoryMetricsDto implements Serializable {

    private static final long serialVersionUID = 6393971550408245017L;

    private long heapUsed;
    private long heapCommitted;
    private long heapMax;
    private long nonHeapUsed;
    private long nonHeapCommitted;
    private long nonHeapMax;
    private long directCount;
    private long directUsed;
    private long directMax;
    private long mappedCount;
    private long mappedUsed;
    private long mappedMax;
    private long memorySegmentsAvailable;
    private long memorySegmentsTotal;
    private long nettyShuffleMemorySegmentsAvailable;
    private long nettyShuffleMemorySegmentsUsed;
    private long nettyShuffleMemorySegmentsTotal;
    private long nettyShuffleMemoryAvailable;
    private long nettyShuffleMemoryUsed;
    private long nettyShuffleMemoryTotal;
    private List<GarbageCollectDto> garbageCollectors;
}

