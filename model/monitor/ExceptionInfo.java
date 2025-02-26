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
public class ExceptionInfo extends WarnInfo implements Serializable {

    private long timestamp;
    private String logInfo;
    private String directException;

    @Override
    public String getType() {
        return WarnTypes.EXCEPTION.getDesc();
    }

    @Override
    public String toMsg() {
        return "报错信息：" + logInfo;
    }
}
