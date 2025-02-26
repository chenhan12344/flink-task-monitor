package com.demo.model.monitor;

import com.demo.constants.WarnTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author sky
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContainerExceedPhysicalMemoryLimitInfo extends WarnInfo implements Serializable {

    private long timestamp;
    private String logInfo;

    @Override
    public String getType() {
        return WarnTypes.CONTAINER_EXCEED_PHYS_MEM_LIMIT.getDesc();
    }

    @Override
    public String toMsg() {
        return "超用详情：" + logInfo;
    }
}
