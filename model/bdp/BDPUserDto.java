package com.demo.model.bdp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:20
 */
@Data
public class BDPUserDto implements Serializable {
    private static final long serialVersionUID = -4828748048575336495L;
    private String userId;
    private String userName;
}
