package org.moboxlab.moboxbot.Util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.BasicInfo;

/**
 * 控制台消息日志
 * 只负责把 OneBot 事件与机器人发送内容整理成易读文本。
 */
public class MessageLogUtil {
    private static final int MAX_TEXT_LENGTH = 2000;

    public static void logIncoming(JSONObject json) {
        if (!BasicInfo.messageLog || json == null || BasicInfo.logger == null) return;
        if (isHeartbeat(json)) return;
        String postType = json.getString("post_type");
        if ("message".equals(postType)) {
            logMessage(json);
            return;
        }
        if ("notice".equals(postType)) {
            BasicInfo.logger.sendInfo("[推送] 通知 "+safe(json.getString("notice_type"))
                    +"/"+safe(json.getString("sub_type"))
                    +idPart(" 群",json.getLongValue("group_id"))
                    +idPart(" 用户",json.getLongValue("user_id"))
                    +idPart(" 操作者",json.getLongValue("operator_id"))
                    +idPart(" 目标",json.getLongValue("target_id")));
            return;
        }
        if ("request".equals(postType)) {
            BasicInfo.logger.sendInfo("[推送] 请求 "+safe(json.getString("request_type"))
                    +"/"+safe(json.getString("sub_type"))
                    +idPart(" 群",json.getLongValue("group_id"))
                    +idPart(" 用户",json.getLongValue("user_id"))
                    +textPart(" 备注",json.getString("comment")));
            return;
        }
        if ("meta_event".equals(postType)) {
            JSONObject status = json.getJSONObject("status");
            String state = status == null ? "" : " online="+status.getBooleanValue("online")
                    +" good="+status.getBooleanValue("good");
            BasicInfo.logger.sendInfo("[推送] 元事件 "+safe(json.getString("meta_event_type"))
                    +"/"+safe(json.getString("sub_type"))+state);
            return;
        }
        BasicInfo.logger.sendInfo("[推送] 未知事件："+limit(json.toJSONString(),MAX_TEXT_LENGTH));
    }

    public static boolean isHeartbeat(JSONObject json) {
        if (json == null) return false;
        return "meta_event".equals(json.getString("post_type"))
                && "heartbeat".equals(json.getString("meta_event_type"));
    }

    public static void logOutgoingAction(String action,JSONObject params,JSONObject response) {
        if (!BasicInfo.messageLog || BasicInfo.logger == null) return;
        if (response == null || response.getIntValue("retcode") != 0) return;
        if ("send_group_msg".equals(action)) {
            BasicInfo.logger.sendInfo("[发送] 群 "+paramID(params,"group_id")+"："
                    +formatMessage(paramMessage(params)));
            return;
        }
        if ("send_private_msg".equals(action)) {
            BasicInfo.logger.sendInfo("[发送] 私聊 "+paramID(params,"user_id")+"："
                    +formatMessage(paramMessage(params)));
        }
    }

    public static String formatMessage(JSONArray message) {
        if (message == null || message.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.size(); i++) {
            JSONObject segment = message.getJSONObject(i);
            if (segment == null) continue;
            String type = segment.getString("type");
            JSONObject data = segment.getJSONObject("data");
            if ("text".equals(type)) {
                builder.append(data == null ? "" : safe(data.getString("text")));
            } else if ("image".equals(type)) {
                builder.append("[图片]");
            } else if ("at".equals(type)) {
                builder.append("@").append(data == null ? "" : safe(data.getString("qq")));
            } else if ("reply".equals(type)) {
                builder.append("[回复]");
            } else if ("face".equals(type) || "mface".equals(type)) {
                builder.append("[表情]");
            } else if ("record".equals(type)) {
                builder.append("[语音]");
            } else if ("video".equals(type)) {
                builder.append("[视频]");
            } else if ("file".equals(type)) {
                builder.append("[文件]");
            } else if ("json".equals(type)) {
                builder.append("[卡片]");
            } else if ("forward".equals(type) || "node".equals(type)) {
                builder.append("[合并转发]");
            } else if ("rps".equals(type)) {
                builder.append("[猜拳]");
            } else if ("dice".equals(type)) {
                builder.append("[骰子]");
            } else if ("poke".equals(type)) {
                builder.append("[戳一戳]");
            } else if ("markdown".equals(type)) {
                builder.append("[Markdown]");
            } else if ("keyboard".equals(type)) {
                builder.append("[按钮]");
            } else {
                builder.append("[").append(type == null ? "未知消息" : type).append("]");
            }
        }
        return limit(builder.toString(),MAX_TEXT_LENGTH);
    }

    /**
     * 清理 CQ 码，主要用于消息段缺失时兼容 raw_message。
     */
    public static String formatRawMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) return "";
        String text = rawMessage;
        text = text.replaceAll("(?i)\\[CQ:image[^\\]]*\\]","[图片]");
        text = text.replaceAll("(?i)\\[CQ:face[^\\]]*\\]","[表情]");
        text = text.replaceAll("(?i)\\[CQ:mface[^\\]]*\\]","[表情]");
        text = text.replaceAll("(?i)\\[CQ:record[^\\]]*\\]","[语音]");
        text = text.replaceAll("(?i)\\[CQ:video[^\\]]*\\]","[视频]");
        text = text.replaceAll("(?i)\\[CQ:file[^\\]]*\\]","[文件]");
        text = text.replaceAll("(?i)\\[CQ:json[^\\]]*\\]","[卡片]");
        text = text.replaceAll("(?i)\\[CQ:forward[^\\]]*\\]","[合并转发]");
        text = text.replaceAll("(?i)\\[CQ:at,qq=([^,\\]]+)[^\\]]*\\]","@$1");
        return limit(text,MAX_TEXT_LENGTH);
    }

    private static void logMessage(JSONObject json) {
        String messageType = json.getString("message_type");
        String text = formatMessage(json.getJSONArray("message"));
        if (text.isEmpty()) text = formatRawMessage(json.getString("raw_message"));
        JSONObject sender = json.getJSONObject("sender");
        String senderName = sender == null ? "" : safe(sender.getString("nickname"));
        long userID = json.getLongValue("user_id");
        String userText = senderName.isEmpty() ? String.valueOf(userID) : senderName+"("+userID+")";
        if ("group".equals(messageType)) {
            String groupName = safe(json.getString("group_name"));
            String groupText = groupName.isEmpty()
                    ? String.valueOf(json.getLongValue("group_id"))
                    : groupName+"("+json.getLongValue("group_id")+")";
            BasicInfo.logger.sendInfo("[消息] 群 "+groupText+" 用户 "+userText+"："+text);
            return;
        }
        if ("private".equals(messageType)) {
            BasicInfo.logger.sendInfo("[消息] 私聊 用户 "+userText+"："+text);
            return;
        }
        BasicInfo.logger.sendInfo("[消息] 未知类型 "+safe(messageType)+" 用户 "+userText+"："+text);
    }

    private static JSONArray paramMessage(JSONObject params) {
        if (params == null) return null;
        return params.getJSONArray("message");
    }

    private static String paramID(JSONObject params,String key) {
        if (params == null) return "0";
        String value = params.getString(key);
        return value == null || value.isEmpty() ? "0" : value;
    }

    private static String idPart(String prefix,long value) {
        return value <= 0 ? "" : prefix+value;
    }

    private static String textPart(String prefix,String value) {
        return value == null || value.trim().isEmpty() ? "" : prefix+"："+limit(value,MAX_TEXT_LENGTH);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String limit(String value,int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        return value.substring(0,maxLength)+"...";
    }
}
