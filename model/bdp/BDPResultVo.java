package com.demo.model.bdp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:00
 */
@Data
public class BDPResultVo implements Serializable {
    private static final long serialVersionUID = 7945420739578035938L;
    private Integer status;
    private Integer ok;
    private BDPPageData data;
}
