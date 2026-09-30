package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.OneBot.OneBotMain;
import org.moboxlab.moboxbot.Plugin.PluginManagerImpl;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;
import org.moboxlab.moboxlib.Object.ObjectCommand;
import org.moboxlab.moboxlib.Object.ObjectLogger;

import java.util.ArrayList;
import java.util.List;

public class CommandStatus extends ObjectCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("status");
        prefixList.add("stat");
        return prefixList;
    }

    @Override
    public boolean execute(String[] args,ObjectLogger logger) {
        BasicInfo.logger.sendInfo("MoBoxBot 版本："+BasicInfo.version);
        BasicInfo.logger.sendInfo("OneBot 状态："+OneBotMain.getStateText());
        BasicInfo.logger.sendInfo("聊天命令数量："+CommandRegistry.size());
        if (PluginManagerImpl.get() != null) {
            BasicInfo.logger.sendInfo("插件数量："+PluginManagerImpl.get().getRecords().size());
        }
        return true;
    }
}
