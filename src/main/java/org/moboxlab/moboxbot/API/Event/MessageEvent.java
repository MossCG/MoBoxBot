package org.moboxlab.moboxbot.API.Event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 消息事件基类
 */
public abstract class MessageEvent extends Event {
    private long messageID;
    private long userID;
    private String rawMessage = "";
    private JSONArray message = new JSONArray();
    private JSONObject sender = new JSONObject(true);
    private JSONObject raw = new JSONObject(true);

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getRawMessage() {
        return rawMessage == null ? "" : rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public JSONArray getMessage() {
        return message;
    }

    public void setMessage(JSONArray message) {
        this.message = message == null ? new JSONArray() : message;
    }

    public JSONObject getSender() {
        return sender;
    }

    public void setSender(JSONObject sender) {
        this.sender = sender == null ? new JSONObject(true) : sender;
    }

    public JSONObject getRaw() {
        return raw;
    }

    public void setRaw(JSONObject raw) {
        this.raw = raw == null ? new JSONObject(true) : raw;
    }
}
