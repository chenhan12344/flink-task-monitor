package com.demo.model.bdp;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:17
 */
@Data
public class TaskDto implements Serializable {
    private static final long serialVersionUID = 2602646522121962664L;

    /**
     * BDP Flink任务ID
     */
    private int taskId;
    /**
     * BDP应用ID
     */
    private int appId;
    /**
     * BDP应用名称
     */
    private String appName;
    /**
     * BDP任务名称
     */
    private String name;
    /**
     * 任务类型
     * 1：JStorm
     * 2：Spark Streaming
     * 3：Flink Jar
     * 4：Flink Drag
     * 5：Flink SQL
     */
    private int taskType;
    /**
     * 任务描述
     */
    private String description;
    /**
     * 负责人工号
     */
    private String owner;
    /**
     * 负责人名称
     */
    private String ownerName;
    /**
     * 开发人员工号列表（逗号分隔）
     */
    private String authPeople;
    /**
     * 运维人员工号列表（逗号分隔）
     */
    private String devOps;
    /**
     * 发生OOM时是否进行HeapDump
     */
    private int triggerHeapDump;
    /**
     * 是否删除
     */
    private boolean deleted;
    /**
     * 系统编码
     */
    private String systemCode;
    /**
     * 版本状态
     */
    private int versionStatus;
    /**
     * 编辑状态
     */
    private int editStatus;
    /**
     * 开发人员列表
     */
    private List<BDPUserDto> authUsers;
    /**
     * 运维人员列表
     */
    private List<BDPUserDto> devOpsUsers;
    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;
    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
