package com.demo.utils;

import com.demo.model.ProcessStatusDto;
import org.apache.commons.lang3.SystemUtils;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * 操作系统工具类
 * 用于执行OS层面命令、获取OS层面指标等操作
 *
 * @author sky
 */
public class OsUtil {

    private static final int UNIX_PS_FIELD_PID = 0;
    private static final int UNIX_PS_FIELD_USER = 1;
    private static final int UNIX_PS_FIELD_PR = 2;
    private static final int UNIX_PS_FIELD_NI = 3;
    private static final int UNIX_PS_FIELD_VIRT = 4;
    private static final int UNIX_PS_FIELD_RES = 5;
    private static final int UNIX_PS_FIELD_S = 6;
    private static final int UNIX_PS_FIELD_CPU = 7;
    private static final int UNIX_PS_FIELD_MEM = 8;

    /**
     * 获取当前JVM进程ID号
     *
     * @return
     */
    public static long getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(name.split("@")[0]);
    }

    /**
     * 获取当前JVM的进程状态
     *
     * @return 当前JVM的进程状态
     * @throws Exception
     */
    public static ProcessStatusDto getCurrentProcessStatus() throws Exception {
        long pid = getPid();
        return getPlatformRelatedProcessStatus(pid);
    }

    private static ProcessStatusDto getPlatformRelatedProcessStatus(long pid) throws Exception {

        long currentTimeMillis = System.currentTimeMillis();

        if (SystemUtils.IS_OS_WINDOWS) {
            String cmd = String.format("tasklist /FI \"PID eq %s\"", pid);
            List<String> cmdResults = ShellUtil.execShellAndGetResults(cmd);
            String psResult = cmdResults.get(3).trim();
            String[] elements = psResult.split("\\s+");
            ProcessStatusDto dto = new ProcessStatusDto();
            String memSize = elements[4].replace(",", "");
            dto.setResidentMemorySize(Long.parseLong(memSize));
            return dto;
        } else {
            String cmd = String.format("ps -axo pid,user,pri,ni,vsz,rss,s,%%cpu,%%mem -q %s | tail -n 1", pid);
            if (SystemUtils.IS_OS_MAC) {
                cmd = String.format("ps -axo pid,user,pri,ni,vsz,rss,state,%%cpu,%%mem -p %s | tail -n 1", pid);
            }
            List<String> cmdResults = ShellUtil.execShellAndGetResults(cmd);
            String psResult = cmdResults.get(0).trim();

            String[] elements = psResult.split("\\s+");
            ProcessStatusDto dto = new ProcessStatusDto();
            dto.setTimestamp(currentTimeMillis);
            dto.setPid(pid);
            dto.setPriority(Integer.valueOf(elements[UNIX_PS_FIELD_PR]));
            dto.setNice(Integer.valueOf(elements[UNIX_PS_FIELD_NI]));
            dto.setVirtualMemorySize((Long.parseLong(elements[UNIX_PS_FIELD_VIRT]) << 10));
            dto.setResidentMemorySize((Long.parseLong(elements[UNIX_PS_FIELD_RES]) << 10));
            dto.setCpuUsePercentage(Double.valueOf(elements[UNIX_PS_FIELD_CPU]));
            dto.setMemoryUsePercentage(Double.valueOf(elements[UNIX_PS_FIELD_MEM]));
            return dto;
        }
    }
}
