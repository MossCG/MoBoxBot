package org.moboxlab.moboxbot.Plugin;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 插件类加载器（每个插件一个）
 * 父优先宽松模式，API 与 fastjson 由主程序提供。
 */
public class PluginClassLoader extends URLClassLoader {
    private final String pluginName;

    public PluginClassLoader(URL[] urls,ClassLoader parent,String pluginName) {
        super(urls,parent);
        this.pluginName = pluginName == null ? "未知插件" : pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
