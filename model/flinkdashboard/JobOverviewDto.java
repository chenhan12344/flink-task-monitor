package com.demo.model.flinkdashboard;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author sky
 */

@Data
public class JobOverviewDto {

    private Long duration;

    private String jid;

    private String name;

    @JSONField(name = "start-time")
    private Long startTime;

    private Timestamps timestamps;

    private List<VertexDto> vertices;

    /**
     * 作业运各行状态的时间戳
     */
    @Data
    public static class Timestamps {

        @JSONField(name = "FAILING")
        private Long failing;

        @JSONField(name = "RUNNING")
        private Long running;

        @JSONField(name = "FINISHED")
        private Long finished;

        @JSONField(name = "CREATED")
        private Long created;

        @JSONField(name = "SUSPENDED")
        private Long suspended;

        @JSONField(name = "INITIALIZING")
        private Long initializing;

        @JSONField(name = "CANCELLING")
        private Long cancelling;

        @JSONField(name = "CANCELED")
        private Long canceled;

        @JSONField(name = "RECONCILING")
        private Long reconciling;

        @JSONField(name = "FAILED")
        private Long failed;

        @JSONField(name = "RESTARTING")
        private Long restarting;

    }
}
