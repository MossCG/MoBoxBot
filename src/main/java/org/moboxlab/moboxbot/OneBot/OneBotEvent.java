package org.moboxlab.moboxbot.OneBot;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.Event.GroupMessageEvent;
import org.moboxlab.moboxbot.API.Event.MetaEvent;
import org.moboxlab.moboxbot.API.Event.NoticeEvent;
import org.moboxlab.moboxbot.API.Event.PrivateMessageEvent;
import org.moboxlab.moboxbot.API.Event.RawEvent;
import org.moboxlab.moboxbot.API.Event.RequestEvent;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Plugin.CommandSenderImpl;
import org.moboxlab.moboxbot.Plugin.PluginManagerImpl;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;
import org.moboxlab.moboxbot.Task.SchedulerService;
import org.moboxlab.moboxbot.Util.CommandUtil;
import org.moboxlab.moboxbot.Util.MuteService;

/**
 * OneBot 事件解析与分发
 */
public class OneBotEvent {
    public static void handle(JSONObject json) {
        if (json == null) return;
        if (json.getString("echo") != null) {
            OneBotEcho.complete(json);
            return;
        }
        String postType = json.getString("post_type");
        if ("message".equals(postType)) {
            handleMessage(json);
            return;
        }
        if ("notice".equals(postType)) {
            if (MuteService.isMuted()) return;
            NoticeEvent event = new NoticeEvent();
            event.setNoticeType(json.getString("notice_type"));
            event.setSubType(json.getString("sub_type"));
            event.setUserID(json.getLongValue("user_id"));
            event.setGroupID(json.getLongValue("group_id"));
            event.setOperatorID(json.getLongValue("operator_id"));
            event.setTargetID(json.getLongValue("target_id"));
            event.setRaw(json);
            dispatch(event);
            return;
        }
        if ("request".equals(postType)) {
            if (MuteService.isMuted()) return;
            RequestEvent event = new RequestEvent();
            event.setRequestType(json.getString("request_type"));
            event.setSubType(json.getString("sub_type"));
            event.setUserID(json.getLongValue("user_id"));
            event.setGroupID(json.getLongValue("group_id"));
            event.setComment(json.getString("comment"));
            event.setFlag(json.getString("flag"));
            event.setRaw(json);
            dispatch(event);
            return;
        }
        if ("meta_event".equals(postType)) {
            OneBotMain.markHeartbeat();
            if (MuteService.isMuted()) return;
            MetaEvent event = new MetaEvent();
            event.setMetaEventType(json.getString("meta_event_type"));
            event.setSubType(json.getString("sub_type"));
            event.setTime(json.getLongValue("time"));
            event.setInterval(json.getLongValue("interval"));
            event.setStatus(json.getJSONObject("status"));
            event.setRaw(json);
            dispatch(event);
            return;
        }
        if (MuteService.isMuted()) return;
        dispatch(new RawEvent(json));
    }

    private static void handleMessage(JSONObject json) {
        String messageType = json.getString("message_type");
        if ("group".equals(messageType)) {
            GroupMessageEvent event = new GroupMessageEvent();
            fillMessage(event,json);
            event.setGroupID(json.getLongValue("group_id"));
            String rawMessage = CommandUtil.normalizeCommandMessage(event.getRawMessage(),json.getLongValue("self_id"));
            CommandSenderImpl sender = CommandSenderImpl.fromGroup(json);
            if (MuteService.handle(sender,rawMessage)) return;
            SchedulerService.runTaskAsync(() -> {
                CommandRegistry.dispatch(sender,rawMessage);
                PluginManagerImpl.get().getEventBus().callEvent(event);
            });
            return;
        }
        PrivateMessageEvent event = new PrivateMessageEvent();
        fillMessage(event,json);
        String rawMessage = CommandUtil.normalizeCommandMessage(event.getRawMessage(),json.getLongValue("self_id"));
        CommandSenderImpl sender = CommandSenderImpl.fromPrivate(json);
        if (MuteService.handle(sender,rawMessage)) return;
        SchedulerService.runTaskAsync(() -> {
            CommandRegistry.dispatch(sender,rawMessage);
            PluginManagerImpl.get().getEventBus().callEvent(event);
        });
    }

    private static void fillMessage(org.moboxlab.moboxbot.API.Event.MessageEvent event,JSONObject json) {
        event.setMessageID(json.getLongValue("message_id"));
        event.setUserID(json.getLongValue("user_id"));
        event.setRawMessage(json.getString("raw_message"));
        event.setMessage(json.getJSONArray("message"));
        event.setSender(json.getJSONObject("sender"));
        event.setRaw(json);
    }

    private static void dispatch(org.moboxlab.moboxbot.API.Event.Event event) {
        if (PluginManagerImpl.get() == null) return;
        PluginManagerImpl.get().getEventBus().callEventAsync(event);
    }
}
