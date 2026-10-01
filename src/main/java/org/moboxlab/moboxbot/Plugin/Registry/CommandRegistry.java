package org.moboxlab.moboxbot.Plugin.Registry;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Command.CommandInfo;
import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;
import org.moboxlab.moboxbot.API.Plugin;
import org.moboxlab.moboxbot.BasicInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 聊天命令注册表
 */
public class CommandRegistry {
    private static final Map<String,Entry> commandMap = new LinkedHashMap<>();
    private static final Map<String,Long> cooldownMap = new LinkedHashMap<>();

    private static class Entry {
        private final Plugin plugin;
        private final String ownerName;
        private final BotCommand command;

        private Entry(Plugin plugin,String ownerName,BotCommand command) {
            this.plugin = plugin;
            this.ownerName = ownerName;
            this.command = command;
        }
    }

    public static void register(Plugin plugin,BotCommand command) {
        if (plugin == null) return;
        register(plugin.getName(),plugin,command);
    }

    public static void register(BotCommand command) {
        register("MoBoxBot",null,command);
    }

    public static void register(String ownerName,Plugin plugin,BotCommand command) {
        if (command == null) return;
        String owner = ownerName == null ? "未知来源" : ownerName;
        List<String> prefixList = command.prefix();
        if (prefixList == null || prefixList.isEmpty()) return;
        for (String prefix : prefixList) {
            if (prefix == null || prefix.trim().isEmpty()) continue;
            String key = prefix.toLowerCase();
            if (commandMap.containsKey(key)) {
                BasicInfo.logger.sendWarn("聊天命令冲突，将使用后注册的实现："+key+"（来源："+owner+"）");
            }
            commandMap.put(key,new Entry(plugin,owner,command));
        }
        BasicInfo.logger.sendInfo(owner+" 注册了聊天命令："+String.join(",",prefixList));
    }

    public static int unregister(Plugin plugin) {
        if (plugin == null) return 0;
        int count = 0;
        List<String> removeKeys = new ArrayList<>();
        for (Map.Entry<String,Entry> entry : commandMap.entrySet()) {
            if (entry.getValue().plugin == plugin) removeKeys.add(entry.getKey());
        }
        for (String key : removeKeys) {
            commandMap.remove(key);
            count++;
        }
        return count;
    }

    /**
     * 分发聊天命令
     * @return 是否命中命令
     */
    public static boolean dispatch(CommandSender sender,String rawMessage) {
        if (sender == null || rawMessage == null) return false;
        String prefix = sender.getCommandPrefix();
        if (prefix == null || prefix.isEmpty()) prefix = BasicInfo.getConfigString("commandPrefix","/");
        String message = rawMessage.trim();
        if (!message.startsWith(prefix)) return false;
        message = message.substring(prefix.length()).trim();
        if (message.isEmpty()) return false;
        String[] split = message.split("\\s+");
        if (split.length == 0) return false;
        Entry entry = commandMap.get(split[0].toLowerCase());
        if (entry == null) return false;

        BotCommand command = entry.command;
        if (!hasPermission(sender,command.permission())) {
            sender.sendMessage("你没有权限使用这个命令！");
            return true;
        }
        if (!checkCooldown(sender,command)) {
            sender.sendMessage("命令冷却中，请稍后再试！");
            return true;
        }
        try {
            command.execute(sender,split);
        } catch (Throwable throwable) {
            BasicInfo.logger.sendWarn("插件 "+entry.plugin.getName()+" 执行命令 "+split[0]+" 失败："+throwable);
            BasicInfo.sendException(throwable);
            sender.sendMessage("命令执行失败，请联系管理员查看日志！");
        }
        return true;
    }

    public static boolean hasPermission(CommandSender sender,CommandPermission permission) {
        if (sender == null || permission == null) return false;
        if (permission == CommandPermission.EVERYONE) return true;
        if (permission == CommandPermission.OWNER) return isOwner(sender.getUserID());
        if (permission == CommandPermission.BOT_ADMIN) return isBotAdmin(sender.getUserID());
        if (permission == CommandPermission.GROUP_OWNER) return sender.hasPermission(CommandPermission.GROUP_OWNER);
        if (permission == CommandPermission.GROUP_ADMIN) return sender.hasPermission(CommandPermission.GROUP_ADMIN);
        return false;
    }

    public static int size() {
        return commandMap.size();
    }

    public static int count(Plugin plugin) {
        int count = 0;
        for (Entry entry : commandMap.values()) {
            if (entry.plugin == plugin) count++;
        }
        return count;
    }

    public static List<CommandInfo> getCommandInfoList() {
        List<CommandInfo> result = new ArrayList<>();
        Set<BotCommand> seen = new HashSet<>();
        for (Entry entry : commandMap.values()) {
            if (entry.command == null || seen.contains(entry.command)) continue;
            seen.add(entry.command);
            List<String> prefixList = entry.command.prefix();
            if (prefixList == null || prefixList.isEmpty()) continue;
            String name = prefixList.get(0);
            List<String> aliases = new ArrayList<>();
            for (int i = 1; i < prefixList.size(); i++) {
                if (prefixList.get(i) != null && !prefixList.get(i).isEmpty()) aliases.add(prefixList.get(i));
            }
            result.add(new CommandInfo(name,aliases,entry.command.description(),entry.command.permission(),entry.ownerName));
        }
        return result;
    }

    private static boolean checkCooldown(CommandSender sender,BotCommand command) {
        int cooldown = command.cooldownSeconds();
        if (cooldown <= 0) return true;
        String key = command.getClass().getName()+"|"+sender.getUserID()+"|"+sender.getGroupID();
        long now = System.currentTimeMillis();
        Long last = cooldownMap.get(key);
        if (last != null && now - last < cooldown*1000L) return false;
        cooldownMap.put(key,now);
        return true;
    }

    private static boolean isOwner(long userID) {
        String owner = BasicInfo.getConfigString("botOwner","");
        if (owner == null || owner.trim().isEmpty()) return false;
        try {
            return Long.parseLong(owner.trim()) == userID;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBotAdmin(long userID) {
        if (isOwner(userID)) return true;
        String admins = BasicInfo.getConfigString("botAdmin","");
        if (admins == null || admins.trim().isEmpty()) return false;
        for (String admin : admins.split(",")) {
            try {
                if (Long.parseLong(admin.trim()) == userID) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
