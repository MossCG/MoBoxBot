package org.moboxlab.moboxbot.API.Event;

public class GroupMessageEvent extends MessageEvent {
    private long groupID;

    public String getName() {
        return "GroupMessageEvent";
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }
}
