package com.demo.model.flinkdashboard;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class MathIndexDto implements Serializable {

    private static final long serialVersionUID = 2879012729956260330L;

    private String id;
    private Double min;
    private Double max;
    private Double avg;
    private Double sum;
}

