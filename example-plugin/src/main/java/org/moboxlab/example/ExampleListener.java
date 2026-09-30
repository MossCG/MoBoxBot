package org.moboxlab.example;

import org.moboxlab.moboxbot.API.Event.EventHandler;
import org.moboxlab.moboxbot.API.Event.EventPriority;
import org.moboxlab.moboxbot.API.Event.GroupMessageEvent;
import org.moboxlab.moboxbot.API.Event.Listener;
import org.moboxlab.moboxbot.API.Event.PrivateMessageEvent;
import org.moboxlab.moboxbot.API.OneBot.MessageUtil;

public class ExampleListener implements Listener {
    private final ExamplePlugin plugin;

    public ExampleListener(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGroupMessage(GroupMessageEvent event) {
        plugin.getLogger().sendInfo("收到群消息："+event.getGroupID()+" / "+event.getUserID()+" / "+event.getRawMessage());
        if (plugin.getConfig().getBoolean("enableWelcome",true)
                && "hello".equalsIgnoreCase(event.getRawMessage())) {
            plugin.getServer().getOneBotClient().sendGroupMessage(
                    event.getGroupID(),
                    MessageUtil.message(MessageUtil.text(plugin.getConfig().getString("welcomeText","你好！"))));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrivateMessage(PrivateMessageEvent event) {
        plugin.getLogger().sendInfo("收到私聊消息："+event.getUserID()+" / "+event.getRawMessage());
    }
}
