package org.moboxlab.moboxbot.Util;

import org.moboxlab.moboxbot.BasicInfo;

/**
 * 命令文本工具
 * 支持 @机器人 后接命令，例如 @MoBoxBot /ping 或 @MoBoxBot ping。
 */
public class CommandUtil {
    public static String normalizeCommandMessage(String rawMessage,long selfID) {
        if (rawMessage == null) return null;
        String message = rawMessage.trim();
        if (message.isEmpty()) return message;

        boolean stripped = false;
        if (selfID > 0) {
            String[] mentions = new String[]{
                    "[CQ:at,qq="+selfID+"]",
                    "[CQ:at,qq=\""+selfID+"\"]",
                    "@"+selfID
            };
            boolean changed = true;
            while (changed) {
                changed = false;
                for (String mention : mentions) {
                    if (message.regionMatches(true,0,mention,0,mention.length())) {
                        message = message.substring(mention.length()).trim();
                        stripped = true;
                        changed = true;
                        break;
                    }
                }
            }
        }

        if (stripped && !message.isEmpty()) {
            String prefix = BasicInfo.getConfigString("commandPrefix","/");
            if (prefix == null || prefix.isEmpty()) prefix = "/";
            if (!message.startsWith(prefix)) message = prefix + message;
        }
        return message;
    }
}
