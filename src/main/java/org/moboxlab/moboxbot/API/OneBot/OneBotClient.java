package org.moboxlab.moboxbot.API.OneBot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * OneBot 客户端
 * 所有主动调用统一走这里，调用失败时返回原始响应对象或 null。
 */
public interface OneBotClient {
    JSONObject sendGroupMessage(long groupID,JSONArray message);

    JSONObject sendPrivateMessage(long userID,JSONArray message);

    JSONObject deleteMessage(long messageID);

    JSONObject getGroupList();

    JSONObject getGroupMemberInfo(long groupID,long userID);

    JSONObject setGroupBan(long groupID,long userID,long duration);

    JSONObject callAction(String action,JSONObject params);

    boolean isConnected();
}
