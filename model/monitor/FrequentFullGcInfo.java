package com.demo.model.monitor;

import com.demo.utils.DateUtil;
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
public class FrequentFullGcInfo extends WarnInfo implements Serializable {

    private static final DecimalFormat FREQ_FORMAT = new DecimalFormat("#");

    /**
     * FullGC次数增量
     */
    private long deltaFullGcCount;

    /**
     * 运行时间增量（毫秒）
     */
    private long deltaRunningDurationMs;

    /**
     * 总FullGC次数
     */
    private long totalFullGcCount;

    /**
     * 总FullGC时间
     */
    private long totalFullGcTimeMs;

    /**
     * TaskManager容器ID
     */
    private String containerId;

    @Override
    public String getType() {
        return WarnTypes.FREQUENT_FULL_GC.getDesc();
    }

    @Override
    public String toMsg() {
        return "GC频繁：" + "容器" + containerId + "近" + DateUtil.ms2desc(deltaRunningDurationMs) + "内发生了" + deltaFullGcCount + "次FullGC；"
                + "总GC次数：" + totalFullGcTimeMs + "，总GC时间" + DateUtil.ms2desc(totalFullGcTimeMs);
    }
}
