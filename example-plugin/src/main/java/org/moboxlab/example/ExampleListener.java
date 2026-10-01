package org.moboxlab.example;

import org.moboxlab.moboxbot.API.Event.EventHandler;
import org.moboxlab.moboxbot.API.Event.EventPriority;
import org.moboxlab.moboxbot.API.Event.GroupMessageEvent;
import org.moboxlab.moboxbot.API.Event.Listener;
import org.moboxlab.moboxbot.API.Event.PrivateMessageEvent;

public class ExampleListener implements Listener {
    private final ExamplePlugin plugin;

    public ExampleListener(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGroupMessage(GroupMessageEvent event) {
        plugin.getLogger().sendInfo("收到群消息："+event.getGroupID()+" / "+event.getUserID()+" / "+event.getRawMessage());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrivateMessage(PrivateMessageEvent event) {
        plugin.getLogger().sendInfo("收到私聊消息："+event.getUserID()+" / "+event.getRawMessage());
    }
}
