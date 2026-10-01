package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;
import org.moboxlab.moboxbot.Util.MuteService;

import java.util.ArrayList;
import java.util.List;

/**
 * 内置闭麦命令元数据与实际入口
 */
public class MuteCommand extends BotCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("quiet");
        prefixList.add("muteself");
        prefixList.add("selfmute");
        return prefixList;
    }

    @Override
    public CommandPermission permission() {
        return CommandPermission.BOT_ADMIN;
    }

    @Override
    public String description() {
        return "切换闭麦状态";
    }

    @Override
    public boolean execute(CommandSender sender,String[] args) {
        MuteService.toggle(sender);
        return true;
    }
}
