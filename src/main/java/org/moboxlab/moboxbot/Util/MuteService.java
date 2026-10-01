package org.moboxlab.moboxbot.Util;

import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;

/**
 * 内置闭麦服务
 * 闭麦后不响应任何命令、消息和事件，只接受下一次管理员解除命令。
 */
public class MuteService {
    private static volatile boolean muted = false;

    public static boolean isMuted() {
        return muted;
    }

    /**
     * 处理闭麦开关命令。
     * @return true 表示消息已被闭麦逻辑处理，不再进入命令和事件分发
     */
    public static boolean handle(CommandSender sender,String rawMessage) {
        String command = parseCommand(rawMessage);
        boolean muteCommand = isMuteCommand(command);

        if (muted && !muteCommand) return true;
        if (!muteCommand) return false;

        if (!CommandRegistry.hasPermission(sender,CommandPermission.BOT_ADMIN)) {
            if (!muted) sender.sendMessage("你没有权限使用这个命令！");
            return true;
        }

        muted = !muted;
        if (muted) {
            sender.sendMessage("已闭麦，只响应下一次解除命令！");
            BasicInfo.logger.sendInfo("已开启闭麦模式！");
        } else {
            sender.sendMessage("已解除闭麦！");
            BasicInfo.logger.sendInfo("已解除闭麦模式！");
        }
        return true;
    }

    private static boolean isMuteCommand(String command) {
        if (command == null) return false;
        return "quiet".equals(command)
                || "muteself".equals(command)
                || "selfmute".equals(command);
    }

    private static String parseCommand(String rawMessage) {
        if (rawMessage == null) return null;
        String prefix = BasicInfo.getConfigString("commandPrefix","/");
        if (prefix == null || prefix.isEmpty()) prefix = "/";
        String message = rawMessage.trim();
        if (!message.startsWith(prefix)) return null;
        message = message.substring(prefix.length()).trim();
        if (message.isEmpty()) return null;
        String[] split = message.split("\\s+");
        if (split.length == 0) return null;
        return split[0].toLowerCase();
    }
}
