package com.demo.model.monitor;

import com.demo.utils.DateUtil;
import com.demo.constants.WarnTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author sky
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContinuousCheckpointFailingInfo extends WarnInfo implements Serializable {

    /**
     * 连续失败次数
     */
    private int continuousFailedCount;

    /**
     * 最近一次失败CP的触发时间戳
     */
    private long triggerTimestamp;

    /**
     * 最近一次失败CP花费的时长（毫秒）
     */
    private long recentFailedCheckpointDuration;

    @Override
    public String getType() {
        return WarnTypes.CONTINUOUS_CHECKPOINT_FAILING.getDesc();
    }

    @Override
    public String toMsg() {
        return "CK失败详情：CK连续失败" + continuousFailedCount + "次, 最近一次" + DateUtil.long2DateStr(triggerTimestamp) + "触发, 失败耗时" + DateUtil.ms2desc(recentFailedCheckpointDuration);
    }
}
