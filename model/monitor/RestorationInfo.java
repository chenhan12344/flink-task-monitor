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
public class RestorationInfo extends WarnInfo implements Serializable {

    private int deltaRestoredTimes;
    private int restoredTimes;

    /**
     * 最近一次拉起的时间戳
     */
    private Long latestRestoredTimestamp;
    private Boolean isSavepoint;

    @Override
    public String getType() {
        return WarnTypes.FREQUENT_RESTORING.getDesc();
    }

    @Override
    public String toMsg() {
        StringBuilder msgBuilder = new StringBuilder("拉起详情：任务累计拉起 ")
                .append(restoredTimes).append(" 次");
        if (latestRestoredTimestamp != null) {
            msgBuilder.append("，最近一次拉起时间：").append(DateUtil.long2DateStr(latestRestoredTimestamp));
        }
        if (isSavepoint != null) {
            msgBuilder.append("，拉起类型：").append(isSavepoint ? "Savepoint" : "Checkpoint");
        }

        return msgBuilder.toString();
    }
}
