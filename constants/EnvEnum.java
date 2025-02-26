package com.demo.constants;


import java.util.Arrays;
import java.util.List;

/**
 * 环境枚举
 *
 * @date 2024-03-04
 */
public class EnvEnum {
    final public static String ENV_KEY = "profile";

    final public static String DEV = "dev";
    final public static String SIT = "sit";
    final public static String PAR = "par";
    final public static String PROD = "prod";

    public static List<String> getEnvList() {
        return Arrays.asList(DEV, SIT, PAR, PROD);
    }

    public static List<String> getBDPEnvList() {
        return Arrays.asList(PAR, PROD);
    }
}
