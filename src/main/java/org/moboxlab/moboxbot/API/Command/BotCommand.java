package org.moboxlab.moboxbot.API.Command;

import java.util.Collections;
import java.util.List;

/**
 * 聊天命令
 */
public abstract class BotCommand {
    public abstract List<String> prefix();

    public CommandPermission permission() {
        return CommandPermission.EVERYONE;
    }

    public int cooldownSeconds() {
        return 0;
    }

    public String description() {
        return "";
    }

    public List<String> usage() {
        return Collections.emptyList();
    }

    public abstract boolean execute(CommandSender sender,String[] args);
}
