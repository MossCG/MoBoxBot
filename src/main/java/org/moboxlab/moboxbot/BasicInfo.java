package org.moboxlab.moboxbot;

import org.moboxlab.moboxbot.API.OneBot.OneBotClient;
import org.moboxlab.moboxbot.API.PluginManager;
import org.moboxlab.moboxlib.Object.ObjectConfig;
import org.moboxlab.moboxlib.Object.ObjectLogger;

public class BasicInfo {
    public static String version = "V0.3.0.0.2312";
    public static String author = "MossCG";
    public static final String runDir = "./MoBoxBot";

    public static ObjectLogger logger;
    public static ObjectConfig config;
    public static PluginManager pluginManager;
    public static OneBotClient oneBotClient;

    public static long startTime = 0L;
    public static boolean debug = false;

    public static void sendDebug(String message) {
        if (logger == null) return;
        logger.sendAPI(message,debug);
    }

    public static void sendException(Throwable throwable) {
        if (logger == null || throwable == null) return;
        logger.sendException(throwable);
    }

    public static String getConfigString(String key,String defaultValue) {
        try {
            if (config == null) return defaultValue;
            String value = config.getString(key);
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getConfigInt(String key,int defaultValue) {
        try {
            if (config == null) return defaultValue;
            Integer value = config.getInteger(key);
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getConfigLong(String key,long defaultValue) {
        try {
            if (config == null) return defaultValue;
            String value = config.getString(key);
            return value == null ? defaultValue : Long.parseLong(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getConfigBoolean(String key,boolean defaultValue) {
        try {
            if (config == null) return defaultValue;
            Boolean value = config.getBoolean(key);
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
