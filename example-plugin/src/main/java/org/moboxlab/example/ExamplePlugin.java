package org.moboxlab.example;

import org.moboxlab.moboxbot.API.Plugin;

/**
 * MoBoxBot 示例插件
 */
public class ExamplePlugin extends Plugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
        getLogger().sendInfo("示例插件正在加载，配置路径："+getConfig().getPath());
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerListener(this,new ExampleListener(this));
        getServer().getPluginManager().registerCommand(this,new PingCommand());
        getLogger().sendInfo("示例插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().sendInfo("示例插件已停用！");
    }
}
