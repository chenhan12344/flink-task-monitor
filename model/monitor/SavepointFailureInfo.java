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
public class SavepointFailureInfo extends WarnInfo implements Serializable {

    /**
     * SP触发的时间戳
     */
    private long triggerTimestamp;

    /**
     * SP花费的时长（毫秒）
     */
    private long duration;

    @Override
    public String getType() {
        return WarnTypes.SAVEPOINT_FAILED.getDesc();
    }

    @Override
    public String toMsg() {
        return "SP失败详情：" + DateUtil.long2DateStr(triggerTimestamp) + " 触发, 耗时" + DateUtil.ms2desc(duration);
    }
}
