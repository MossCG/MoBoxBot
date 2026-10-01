package org.moboxlab.moboxbot.API;

import org.moboxlab.moboxbot.API.Event.EventBus;
import org.moboxlab.moboxbot.API.Command.CommandInfo;
import org.moboxlab.moboxbot.API.OneBot.OneBotClient;
import org.moboxlab.moboxbot.API.Storage.StorageService;

import java.util.List;

/**
 * 主程序服务门面
 * 插件从 getServer() 拿这一切，不要直接访问 BasicInfo。
 * API 0.1 冻结后，本接口只允许非破坏性追加方法。
 */
public interface Server {
    String getVersion();

    String getApiVersion();

    String getBotName();

    PluginManager getPluginManager();

    EventBus getEventBus();

    OneBotClient getOneBotClient();

    StorageService getStorage();

    /**
     * 重载主程序配置。
     */
    void reloadConfig();

    /**
     * 获取当前已注册命令元数据。
     */
    List<CommandInfo> getCommandList();

    List<Long> getAdminList();

    boolean addAdmin(long userID);

    boolean removeAdmin(long userID);

    List<PluginInfo> getPluginInfoList();
}
