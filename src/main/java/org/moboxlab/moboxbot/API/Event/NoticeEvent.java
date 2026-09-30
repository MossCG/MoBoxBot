package org.moboxlab.moboxbot.API.Event;

import com.alibaba.fastjson.JSONObject;

public class NoticeEvent extends Event {
    private String noticeType = "";
    private String subType = "";
    private long userID;
    private long groupID;
    private long operatorID;
    private long targetID;
    private JSONObject raw = new JSONObject(true);

    public String getName() {
        return "NoticeEvent";
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
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

    public long getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(long operatorID) {
        this.operatorID = operatorID;
    }

    public long getTargetID() {
        return targetID;
    }

    public void setTargetID(long targetID) {
        this.targetID = targetID;
    }

    public JSONObject getRaw() {
        return raw;
    }

    public void setRaw(JSONObject raw) {
        this.raw = raw == null ? new JSONObject(true) : raw;
    }
}
