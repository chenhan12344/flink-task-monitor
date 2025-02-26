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
public class CheckpointSlowInfo extends WarnInfo implements Serializable {

    private boolean isSavepoint;
    private long duration;

    @Override
    public String getType() {
        return WarnTypes.CHECKPOINT_SLOW.getDesc();
    }

    @Override
    public String toMsg() {
        String checkpointType = isSavepoint ? "SP" : "CK";
        return checkpointType + "缓慢，当前" + checkpointType + "耗时：" + DateUtil.ms2desc(duration);
    }
}
