package com.demo.model.bdp;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.demo.constants.BDPConstants.*;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:01
 */
@Data
public class BDPParamDto implements Serializable {
    private static final long serialVersionUID = -2798744263289992021L;

    private Integer appId;
    private List<Integer> taskType = Arrays.asList(RTC_TASK_TYPE_FLINK_JAR, RTC_TASK_TYPE_FLINK_DRAG, RTC_TASK_TYPE_FLINK_SQL);
    private List<String> owner;
    private String taskId;
    private String name;
    private List<String> runStatus = Collections.singletonList(BDPConst.TASK_RUNNING_STATE);
    private String clusterId;
    private String clusterType;
    private int pageNum = 1;
    private int pageSize = 1000;

    public BDPParamDto() {
    }

    public BDPParamDto(String owner) {
        this.owner = Arrays.asList(owner.split(","));
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
