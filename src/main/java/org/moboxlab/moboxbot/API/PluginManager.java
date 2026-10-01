package org.moboxlab.moboxbot.API;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Event.Listener;

import java.util.List;

/**
 * 插件管理器
 * 每个注册方法都带插件自己，主程序按插件记账并回收。
 * API 0.1 冻结后，本接口只允许非破坏性追加方法。
 */
public interface PluginManager {
    Plugin getPlugin(String name);

    List<Plugin> getPlugins();

    boolean isEnabled(String name);

    void registerListener(Plugin plugin,Listener listener);

    void registerCommand(Plugin plugin,BotCommand command);

    void runTask(Plugin plugin,Runnable task);

    void runTaskLater(Plugin plugin,Runnable task,long delaySeconds);

    void runTaskTimer(Plugin plugin,Runnable task,long delaySeconds,long periodSeconds);

    boolean enablePlugin(String name);

    boolean disablePlugin(String name);

    boolean reloadPlugin(String name);
}
