package com.demo.model.monitor;

import com.demo.constants.WarnTypes;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sky
 */
@Data
public class CookieExpiredInfo extends WarnInfo implements Serializable {

    @Override
    public String getType() {
        return WarnTypes.COOKIE_EXPIRED.getDesc();
    }

    @Override
    public String toMsg() {
        return "BDP平台Cookie过期，无法获取任务列表，请及时更新！";
    }
}
