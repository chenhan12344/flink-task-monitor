package com.demo.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ShellUtil {

    /**
     * 执行Shell命令并获取输出结果
     * ！！ USE WITH CAUTION !!
     * ！！ USE WITH CAUTION !!
     * ！！ USE WITH CAUTION !!
     *
     * @param command Shell命令
     * @return Shell命令结果
     * @throws Exception
     */
    public static List<String> execShellAndGetResults(String command) throws Exception {
        if (StringUtils.isBlank(command)) {
            System.out.println("command is empty");
            return Collections.emptyList();
        }

        String[] commands = SystemUtils.IS_OS_WINDOWS ? new String[]{"cmd.exe", "/c", command} : new String[]{"/bin/bash", "-c", command};

        List<String> results = new ArrayList<>();
        InputStream inputStream = null;
        InputStreamReader isr = null;
        BufferedReader bufferedReader = null;
        try {
            Process exec = Runtime.getRuntime().exec(commands);
            inputStream = exec.getInputStream();
            isr = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                results.add(line);
            }
            exec.waitFor();
        } catch (Exception e) {
            log.error("Exception while executing command: {}", commands);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return results;
    }

    public static void execShell(String shellString) {
        BufferedReader reader;
        try {
            System.out.println("[shell-log]:" + shellString);
            Process process = Runtime.getRuntime().exec(shellString);
            int exitValue = process.waitFor();
            if (0 != exitValue) {
                log.error("Call shell failed. error code is :" + exitValue);
            }
            // 返回值
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[shell-log]:" + line);
            }
        } catch (Throwable e) {
            log.error("Call shell failed. " + e);
        }
        System.out.println("执行完毕");
    }

}
