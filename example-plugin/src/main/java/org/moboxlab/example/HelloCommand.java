package org.moboxlab.example;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例管理员命令
 */
public class HelloCommand extends BotCommand {
    private final ExamplePlugin plugin;

    public HelloCommand(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("hello");
        return prefixList;
    }

    @Override
    public CommandPermission permission() {
        return CommandPermission.BOT_ADMIN;
    }

    @Override
    public String description() {
        return "示例管理员命令";
    }

    @Override
    public boolean execute(CommandSender sender,String[] args) {
        if (!plugin.getConfig().getBoolean("enableWelcome",true)) {
            sender.sendMessage("示例欢迎功能已关闭！");
            return true;
        }
        sender.sendMessage(plugin.getConfig().getString("welcomeText","你好，这里是 MoBoxBot 示例插件！"));
        return true;
    }
}
