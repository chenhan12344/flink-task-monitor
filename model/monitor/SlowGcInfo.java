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
public class SlowGcInfo extends WarnInfo implements Serializable {


    private static final DecimalFormat AVG_GC_TIME_FORMAT = new DecimalFormat("##");

    /**
     * FullGC次数增量
     */
    private long deltaFullGcCount;

    /**
     * FullGC时间增量（毫秒）
     */
    private long deltaGcTimeMs;

    /**
     * TaskManager容器ID
     */
    private String containerId;

    @Override
    public String getType() {
        return WarnTypes.SLOW_GC.getDesc();
    }

    @Override
    public String toMsg() {
        int avgGcTimeSeconds = Math.round(deltaGcTimeMs / 1000.0F / deltaFullGcCount);
        return "GC慢：" + "容器" + containerId + "近" + deltaFullGcCount + "次的平均GC时长为：" + avgGcTimeSeconds + "s";
    }
}
