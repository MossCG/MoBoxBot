package org.moboxlab.moboxbot.API.Event;

import com.alibaba.fastjson.JSONObject;

public class RequestEvent extends Event {
    private String requestType = "";
    private String subType = "";
    private long userID;
    private long groupID;
    private String comment = "";
    private String flag = "";
    private JSONObject raw = new JSONObject(true);

    public String getName() {
        return "RequestEvent";
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public JSONObject getRaw() {
        return raw;
    }

    public void setRaw(JSONObject raw) {
        this.raw = raw == null ? new JSONObject(true) : raw;
    }
}
