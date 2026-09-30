package org.moboxlab.moboxbot.API.Event;

import com.alibaba.fastjson.JSONObject;

public class RawEvent extends Event {
    private JSONObject raw = new JSONObject(true);

    public RawEvent() {
    }

    public RawEvent(JSONObject raw) {
        this.raw = raw == null ? new JSONObject(true) : raw;
    }

    public String getName() {
        return "RawEvent";
    }

    public JSONObject getRaw() {
        return raw;
    }

    public void setRaw(JSONObject raw) {
        this.raw = raw == null ? new JSONObject(true) : raw;
    }
}
