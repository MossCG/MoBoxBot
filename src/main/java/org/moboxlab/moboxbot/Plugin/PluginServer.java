package org.moboxlab.moboxbot.Plugin;

import org.moboxlab.moboxbot.API.Event.EventBus;
import org.moboxlab.moboxbot.API.Command.CommandInfo;
import org.moboxlab.moboxbot.API.MoBoxBotAPI;
import org.moboxlab.moboxbot.API.OneBot.OneBotClient;
import org.moboxlab.moboxbot.API.PluginManager;
import org.moboxlab.moboxbot.API.Server;
import org.moboxlab.moboxbot.API.Storage.StorageService;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Main;
import org.moboxlab.moboxbot.Util.AdminService;

import java.util.List;

/**
 * 主程序服务门面
 * 插件拿到的一切都从这里走。
 */
public class PluginServer implements Server {
    @Override
    public String getVersion() {
        return BasicInfo.version;
    }

    @Override
    public String getApiVersion() {
        return MoBoxBotAPI.API_VERSION;
    }

    @Override
    public String getBotName() {
        return BasicInfo.getConfigString("botName","MoBoxBot");
    }

    @Override
    public PluginManager getPluginManager() {
        return PluginManagerImpl.get();
    }

    @Override
    public EventBus getEventBus() {
        return PluginManagerImpl.get().getEventBus();
    }

    @Override
    public OneBotClient getOneBotClient() {
        return BasicInfo.oneBotClient;
    }

    @Override
    public StorageService getStorage() {
        return PluginManagerImpl.get().getStorage();
    }

    @Override
    public void reloadConfig() {
        Main.reloadConfig();
    }

    @Override
    public List<CommandInfo> getCommandList() {
        return PluginManagerImpl.get().getCommandList();
    }

    @Override
    public List<Long> getAdminList() {
        return AdminService.getAdminList();
    }

    @Override
    public boolean addAdmin(long userID) {
        return AdminService.addAdmin(userID);
    }

    @Override
    public boolean removeAdmin(long userID) {
        return AdminService.removeAdmin(userID);
    }
}
