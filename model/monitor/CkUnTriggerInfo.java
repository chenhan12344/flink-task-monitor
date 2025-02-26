package com.demo.model.monitor;

import com.demo.utils.DateUtil;
import com.demo.constants.WarnTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CkUnTriggerInfo extends WarnInfo implements Serializable {

    private long triggerHour;
    private long triggerTimestamp;

    @Override
    public String getType() {
        return WarnTypes.CK_UN_TRIGGER.getDesc();
    }

    @Override
    public String toMsg() {
        return "未触发详情: 超" + triggerHour + "小时未触发CK/SP, 最近一次触发时间: " + DateUtil.long2DateStr(triggerTimestamp);
    }
}
