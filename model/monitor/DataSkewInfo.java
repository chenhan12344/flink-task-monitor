package com.demo.model.monitor;

import com.demo.constants.WarnTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * @author sky
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSkewInfo extends WarnInfo implements Serializable {

    /**
     * 算子名称
     */
    private String operatorName;
    /**
     * 算子接收到的记录数
     */
    private List<Long> recordsReceived;
    /**
     * 算子瞬间负载
     */
    private double operatorPressure;
    /**
     * 数据倾斜判断阈值
     */
    private double skewThreshold;

    @Override
    public String getType() {
        return WarnTypes.DATA_SKEW.getDesc();
    }

    @Override
    public String toMsg() {
        recordsReceived.sort(Comparator.reverseOrder());
        return "倾斜算子：" + operatorName + "\n" +
                "算子瞬时负载：" + operatorPressure + "%\n" +
                "倾斜判定阈值：" + skewThreshold * 100 + "%\n" +
                "RecordsReceived：" + recordsReceived;
    }
}
