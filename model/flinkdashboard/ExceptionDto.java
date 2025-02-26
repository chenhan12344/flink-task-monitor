package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author sky
 */
@Data
public class ExceptionDto {

    /**
     * 根本异常
     */
    @JSONField(name = "root-exception")
    private String rootException;

    /**
     * 异常时间戳
     */
    private Long timestamp;

    private Boolean truncated;

    @Data
    public static class ExceptionHistory {

        @JSONField(name = "entries")
        private List<ExceptionEntry> exceptionEntries;

        private Boolean truncated;

    }

    @Data
    public static class ExceptionEntry {

        /**
         * 异常名称
         */
        private String exceptionName;

        /**
         * 异常主机
         */
        private String location;

        /**
         * 异常堆栈
         */
        private String stacktrace;

        /**
         * 算子子任务名称
         */
        private String taskName;

        /**
         * 异常时间戳
         */
        private Long timestamp;
    }

}
