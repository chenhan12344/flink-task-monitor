package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 15:31
 */
@Data
public class TaskMetricsDto implements Serializable {

    private static final long serialVersionUID = 2879012729956260330L;

    @JSONField(name = "read-bytes")
    private long readBytes;
    @JSONField(name = "read-bytes-complete")
    private Boolean readBytesComplete;
    @JSONField(name = "write-bytes")
    private long writeBytes;
    @JSONField(name = "write-bytes-complete")
    private Boolean writeBytesComplete;
    @JSONField(name = "read-records")
    private long readRecords;
    @JSONField(name = "read-records-complete")
    private Boolean readRecordsComplete;
    @JSONField(name = "write-records")
    private long writeRecords;
    @JSONField(name = "write-records-complete")
    private Boolean writeRecordsComplete;
}

