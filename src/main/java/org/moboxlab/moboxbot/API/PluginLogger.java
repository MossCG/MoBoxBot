package org.moboxlab.moboxbot.API;

import org.moboxlab.moboxlib.Object.ObjectLogger;

/**
 * 插件日志
 * 输出自动带 [插件名] 前缀。
 */
public class PluginLogger {
    private final String pluginName;
    private final ObjectLogger logger;

    public PluginLogger(String pluginName,ObjectLogger logger) {
        this.pluginName = pluginName == null ? "未知插件" : pluginName;
        this.logger = logger;
    }

    public void sendInfo(String message) {
        if (logger != null) logger.sendInfo("["+pluginName+"] "+message);
    }

    public void sendWarn(String message) {
        if (logger != null) logger.sendWarn("["+pluginName+"] "+message);
    }

    public void sendError(String message) {
        if (logger != null) logger.sendError("["+pluginName+"] "+message);
    }

    public void sendException(Throwable throwable) {
        if (logger != null) logger.sendException(throwable);
    }
}
