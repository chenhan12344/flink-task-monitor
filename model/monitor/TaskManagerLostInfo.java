package com.demo.model.monitor;

import com.demo.constants.WarnTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author sky
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskManagerLostInfo extends WarnInfo implements Serializable {

    private String taskManager;

    @Override
    public String getType() {
        return WarnTypes.TASK_MANAGER_LOST.getDesc();
    }

    @Override
    public String toMsg() {
        return String.format("TaskManager: %s 连接异常发生重启（可能是平台机器问题或内存超用），请关注任务运行状况和JobManager日志", taskManager);
    }
}
