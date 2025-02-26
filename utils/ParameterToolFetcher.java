package com.demo.utils;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.java.utils.ParameterTool;

/**
 * @author sky
 */
public class ParameterToolFetcher {

    public static ParameterTool fetch(AbstractRichFunction userFunction) {
        return (ParameterTool) userFunction.getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
    }


}
