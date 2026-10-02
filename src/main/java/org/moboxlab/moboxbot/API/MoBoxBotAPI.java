package org.moboxlab.moboxbot.API;

/**
 * 插件 API 静态入口
 * 插件只认这个包，不要碰主程序内部实现。
 */
public class MoBoxBotAPI {
    public static final String API_VERSION = "0.2";

    private static Server server;

    public static String getVersion() {
        return server == null ? "" : server.getVersion();
    }

    public static String getApiVersion() {
        return API_VERSION;
    }

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server pluginServer) {
        server = pluginServer;
    }
}
