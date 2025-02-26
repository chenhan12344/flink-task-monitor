package com.demo.model.bdp;

import com.demo.utils.DateUtil;
import com.demo.constants.BDPApis;
import com.demo.model.monitor.WarnInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Description
 * @Author 
 * @Date 2023/10/24 10:20
 */
@Data
public class FlinkWarnMsgDto implements Serializable {
    private static final long serialVersionUID = -4828748048575336495L;

    private static final String LF = "\n";
    private static final String SPACE = " ";
    private static final String TAB = "\t";

    /**
     * BDP应用ID
     */
    private Integer appId;
    /**
     * BDP任务ID
     */
    private Integer taskId;
    /**
     * Yarn集群
     */
    private String clusterName;
    /**
     * BDP任务名称
     */
    private String taskName;
    /**
     * 任务负责人工号
     */
    private String ownerId;
    /**
     * 任务负责人名称
     */
    private String ownerName;

    /**
     * 总运行时长（毫秒）
     */
    private Long taskDuration;
    /**
     * 当前（最近一次拉起到现在）运行时长（毫秒）
     */
    private Long currentDuration;
    /**
     * Flink任务Dashboard跳转链接
     */
    private String flinkDashboardLink;


    private List<WarnInfo> warnInfos = new ArrayList<>();

    public void addWarnInfo(WarnInfo warnInfo) {
        warnInfos.add(warnInfo);
    }

    public void addWarnInfos(Collection<WarnInfo> warnInfos) {
        this.warnInfos.addAll(warnInfos);
    }

    public FlinkWarnMsgDto() {
    }

    public String buildMsg() {
        StringBuilder msg = new StringBuilder("[告警] Flink任务健康状况异常").append(LF);
        msg.append("[类型] ").append(collectExceptionTypes()).append(LF);
        msg.append("[环境] ").append(getEnvByName(taskName)).append(LF);
        msg.append("[时间] ").append(DateUtil.getCurrentTime()).append(LF);
        msg.append("任务ID：").append(taskId).append(LF);
        msg.append("任务名：").append(taskName).append(LF);
        msg.append("责任人：").append(ownerName).append("(").append(ownerId).append(")").append(LF);
        msg.append("所处集群：").append(clusterName).append(LF);
        msg.append("运行时长：").append(DateUtil.ms2desc(currentDuration)).append(" / ").append(DateUtil.ms2desc(taskDuration)).append(LF);

        for (WarnInfo warnInfo : warnInfos) {
            msg.append(warnInfo.toMsg()).append(LF);
        }

        msg.append("前往处理: ").append(getBdpTaskUrl()).append(LF);
        msg.append("任务详情: ").append(flinkDashboardLink);
        return msg.toString();
    }

    private String collectExceptionTypes() {
        List<String> exceptionTypes = new ArrayList<>(warnInfos.size());
        for (WarnInfo warnInfo : warnInfos) {
            exceptionTypes.add(warnInfo.getType());
        }

        return exceptionTypes.isEmpty() ? "-" : String.join(",", exceptionTypes);
    }

    private String getEnvByName(String taskName) {
        taskName = taskName.toLowerCase();
        if ((taskName.contains("_prod_") || taskName.contains("_pro_") || taskName.contains("_prd_")) && !taskName.contains("gray") && !taskName.contains("grey") && !taskName.contains("_dr")) {
            return "生产环境";
        } else if (taskName.contains("gray") || taskName.contains("grey") || taskName.endsWith("par") || taskName.contains("_par") || taskName.contains("_dr")) {
            return "容灾环境";
        } else if (taskName.contains("test")) {
            return "测试环境";
        } else {
            return "生产环境";
        }
    }

    private String getBdpTaskUrl() {
        String url = BDPApis.TASK_INFO_API + "?taskId=" + taskId;
        if (appId != null) {
            return url + "&appId=" + appId;
        } else {
            return url;
        }
    }

}
