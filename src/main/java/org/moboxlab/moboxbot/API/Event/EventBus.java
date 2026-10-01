package org.moboxlab.moboxbot.API.Event;

import org.moboxlab.moboxbot.API.Plugin;

/**
 * 插件事件总线
 * 插件通常通过 PluginManager 注册监听器，不需要直接操作事件总线。
 */
public interface EventBus {
    void register(Plugin plugin,Listener listener);

    void unregister(Plugin plugin,Listener listener);

    void unregisterAll(Plugin plugin);

    Event callEvent(Event event);

    void callEventAsync(Event event);
}
