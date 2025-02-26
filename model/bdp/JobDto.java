package com.demo.model.bdp;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 18:34
 */
@Data
public class JobDto implements Serializable {
    private static final long serialVersionUID = 2602646522121962662L;
    private String jid;
    private String name;
    private String state;
    private Long duration;
}
