package org.moboxlab.moboxbot.API.Command;

import com.alibaba.fastjson.JSONArray;
import org.moboxlab.moboxbot.API.MoBoxBotAPI;
import org.moboxlab.moboxbot.API.OneBot.MessageUtil;
import org.moboxlab.moboxbot.API.OneBot.OneBotClient;

public interface CommandSender {
    boolean isGroup();

    long getGroupID();

    long getUserID();

    String getName();

    String getCommandPrefix();

    boolean hasPermission(CommandPermission permission);

    void sendMessage(String message);

    void reply(String message);

    /**
     * 发送图片消息，file 支持 OneBot 的 base64://、file:// 或 http:// 地址。
     */
    default void sendImage(String file) {
        if (file == null || file.isEmpty()) return;
        if (MoBoxBotAPI.getServer() == null) return;
        OneBotClient client = MoBoxBotAPI.getServer().getOneBotClient();
        if (client == null) return;
        JSONArray message = MessageUtil.message(MessageUtil.image(file));
        if (isGroup()) {
            client.sendGroupMessage(getGroupID(),message);
        } else {
            client.sendPrivateMessage(getUserID(),message);
        }
    }
}
