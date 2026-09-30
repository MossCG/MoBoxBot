package org.moboxlab.moboxbot.API.Event;

import com.alibaba.fastjson.JSONObject;

public class MetaEvent extends Event {
    private String metaEventType = "";
    private String subType = "";
    private long time;
    private long interval;
    private JSONObject status = new JSONObject(true);
    private JSONObject raw = new JSONObject(true);

    public String getName() {
        return "MetaEvent";
    }

    public String getMetaEventType() {
        return metaEventType;
    }

    public void setMetaEventType(String metaEventType) {
        this.metaEventType = metaEventType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public JSONObject getStatus() {
        return status;
    }

    public void setStatus(JSONObject status) {
        this.status = status == null ? new JSONObject(true) : status;
    }

    public JSONObject getRaw() {
        return raw;
    }

    public void setRaw(JSONObject raw) {
        this.raw = raw == null ? new JSONObject(true) : raw;
    }
}
