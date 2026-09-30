package org.moboxlab.moboxbot.Plugin;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;
import org.moboxlab.moboxbot.API.OneBot.MessageUtil;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;

/**
 * 聊天命令发送者
 */
public class CommandSenderImpl implements CommandSender {
    private final boolean group;
    private final long groupID;
    private final long userID;
    private final String name;
    private final String role;
    private final String commandPrefix;

    public CommandSenderImpl(boolean group,long groupID,long userID,String name,String role) {
        this.group = group;
        this.groupID = groupID;
        this.userID = userID;
        this.name = name == null ? "" : name;
        this.role = role == null ? "member" : role;
        this.commandPrefix = BasicInfo.getConfigString("commandPrefix","/");
    }

    public static CommandSenderImpl fromGroup(JSONObject raw) {
        JSONObject sender = raw.getJSONObject("sender");
        return new CommandSenderImpl(
                true,
                raw.getLongValue("group_id"),
                raw.getLongValue("user_id"),
                sender == null ? "" : sender.getString("nickname"),
                sender == null ? "member" : sender.getString("role"));
    }

    public static CommandSenderImpl fromPrivate(JSONObject raw) {
        JSONObject sender = raw.getJSONObject("sender");
        return new CommandSenderImpl(
                false,
                0L,
                raw.getLongValue("user_id"),
                sender == null ? "" : sender.getString("nickname"),
                "member");
    }

    @Override
    public boolean isGroup() {
        return group;
    }

    @Override
    public long getGroupID() {
        return groupID;
    }

    @Override
    public long getUserID() {
        return userID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCommandPrefix() {
        return commandPrefix;
    }

    @Override
    public boolean hasPermission(CommandPermission permission) {
        if (permission == CommandPermission.GROUP_OWNER) return "owner".equals(role);
        if (permission == CommandPermission.GROUP_ADMIN) return "owner".equals(role) || "admin".equals(role);
        return CommandRegistry.hasPermission(this,permission);
    }

    @Override
    public void sendMessage(String message) {
        if (BasicInfo.oneBotClient == null) {
            BasicInfo.logger.sendWarn("OneBot 客户端尚未就绪，消息发送失败："+message);
            return;
        }
        if (group) {
            BasicInfo.oneBotClient.sendGroupMessage(groupID,MessageUtil.message(MessageUtil.text(message)));
        } else {
            BasicInfo.oneBotClient.sendPrivateMessage(userID,MessageUtil.message(MessageUtil.text(message)));
        }
    }

    @Override
    public void reply(String message) {
        sendMessage(message);
    }
}
