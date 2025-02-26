package com.demo.model.monitor;

import com.demo.constants.WarnTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * @author sky
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PhysicalMemoryReachCriticalLevelInfo extends WarnInfo implements Serializable {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#%");


    private String containerId;

    private long memoryUsedMB;

    private long totalMemoryMB;

    @Override
    public String getType() {
        return WarnTypes.CONTAINER_PHYS_MEM_REACH_CRITICAL_LEVEL.getDesc();
    }

    @Override
    public String toMsg() {
        String memoryUsedPercentage = DECIMAL_FORMAT.format(memoryUsedMB * 1.0 / totalMemoryMB);
        return "容器" + containerId + "已使用" + memoryUsedMB + " MB/" + totalMemoryMB + " MB (" + memoryUsedPercentage + ")";
    }
}
