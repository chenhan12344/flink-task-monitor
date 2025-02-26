package com.demo.metrics;

import org.apache.flink.metrics.Gauge;

/**
 * @author sky
 */
public class PhysicalMemoryGauge implements Gauge<Long> {

    private long size;

    @Override
    public Long getValue() {
        return size;
    }

    @Override
    public String toString() {
        return String.valueOf(size);
    }

    public void update(long size) {
        this.size = size;
    }
}
