package com.demo.model.flinkdashboard;

import lombok.Data;

/**
 * 指标实体类
 *
 * @author sky
 */
@Data
public class Metric<T> {

    private String id;

    private T value;
}
