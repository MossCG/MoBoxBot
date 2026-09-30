package org.moboxlab.moboxbot.Plugin;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Event.Listener;
import org.moboxlab.moboxbot.API.Plugin;
import org.moboxlab.moboxbot.API.PluginDescription;
import org.moboxlab.moboxbot.API.PluginState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 运行期插件记录
 * 插件注册过的资源全部记在这里，停用或重载时按账目回收。
 */
public class PluginRecord {
    public PluginDescription description;
    public Plugin plugin;
    public PluginClassLoader classLoader;
    public PluginState state = PluginState.LOADED;
    public String dataFolder = "";
    public long loadTime = System.currentTimeMillis();
    public String message = "";

    public final List<Listener> listeners = new ArrayList<>();
    public final List<BotCommand> commands = new ArrayList<>();
    public final List<Future<?>> tasks = new ArrayList<>();

    public PluginRecord(PluginDescription description) {
        this.description = description;
    }

    public String getName() {
        return description == null ? "" : description.name;
    }

    public void fail(String reason) {
        this.state = PluginState.FAILED;
        this.message = reason;
        PluginManagerImpl.get().saveRecord(this);
    }
}
