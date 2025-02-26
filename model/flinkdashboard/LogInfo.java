package com.demo.model.flinkdashboard;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author sky
 */
@Data
public class LogInfo implements Serializable {

    private List<LogFile> logs;

    @Data
    public static class LogFile implements Serializable {

        /**
         * 日志文件名
         */
        private String name;
        /**
         * 日志大小（字节）
         */
        private long size;

    }

}
