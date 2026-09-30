package org.moboxlab.moboxbot.OneBot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.OneBot.OneBotClient;

/**
 * OneBot Action 客户端
 */
public class OneBotClientImpl implements OneBotClient {
    @Override
    public JSONObject sendGroupMessage(long groupID,JSONArray message) {
        JSONObject params = new JSONObject(true);
        params.put("group_id",String.valueOf(groupID));
        params.put("message",message);
        return callAction("send_group_msg",params);
    }

    @Override
    public JSONObject sendPrivateMessage(long userID,JSONArray message) {
        JSONObject params = new JSONObject(true);
        params.put("user_id",String.valueOf(userID));
        params.put("message",message);
        return callAction("send_private_msg",params);
    }

    @Override
    public JSONObject deleteMessage(long messageID) {
        JSONObject params = new JSONObject(true);
        params.put("message_id",String.valueOf(messageID));
        return callAction("delete_msg",params);
    }

    @Override
    public JSONObject getGroupList() {
        return callAction("get_group_list",new JSONObject(true));
    }

    @Override
    public JSONObject getGroupMemberInfo(long groupID,long userID) {
        JSONObject params = new JSONObject(true);
        params.put("group_id",String.valueOf(groupID));
        params.put("user_id",String.valueOf(userID));
        return callAction("get_group_member_info",params);
    }

    @Override
    public JSONObject setGroupBan(long groupID,long userID,long duration) {
        JSONObject params = new JSONObject(true);
        params.put("group_id",String.valueOf(groupID));
        params.put("user_id",String.valueOf(userID));
        params.put("duration",duration);
        return callAction("set_group_ban",params);
    }

    @Override
    public JSONObject callAction(String action,JSONObject params) {
        return OneBotMain.sendAction(action,params);
    }

    @Override
    public boolean isConnected() {
        return OneBotMain.isConnected();
    }
}
