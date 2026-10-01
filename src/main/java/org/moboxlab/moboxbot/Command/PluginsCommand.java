package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;
import org.moboxlab.moboxbot.Plugin.PluginManagerImpl;
import org.moboxlab.moboxbot.Plugin.PluginRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 内置聊天命令：显示当前插件列表
 */
public class PluginsCommand extends BotCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("plugins");
        return prefixList;
    }

    @Override
    public CommandPermission permission() {
        return CommandPermission.BOT_ADMIN;
    }

    @Override
    public String description() {
        return "显示当前插件列表";
    }

    @Override
    public boolean execute(CommandSender sender,String[] args) {
        PluginManagerImpl manager = PluginManagerImpl.get();
        if (manager == null) {
            sender.sendMessage("插件内核尚未初始化！");
            return true;
        }
        List<PluginRecord> records = manager.getRecords();
        if (records.isEmpty()) {
            sender.sendMessage("当前没有加载任何插件！");
            return true;
        }
        sender.sendMessage("当前插件列表（"+records.size()+" 个）：");
        for (PluginRecord record : records) {
            sender.sendMessage(
                    "["+record.state.name()+"] "
                    +record.getName()
                    +" "+record.description.version
                    +" | 监听 "+record.listeners.size()
                    +" | 命令 "+record.commands.size()
                    +" | 任务 "+record.tasks.size());
        }
        return true;
    }
}
