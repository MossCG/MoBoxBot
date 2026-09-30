package org.moboxlab.moboxbot.API.OneBot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * OneBot 消息段构造工具
 */
public class MessageUtil {
    public static JSONArray message(JSONObject... segments) {
        JSONArray array = new JSONArray();
        if (segments == null) return array;
        for (JSONObject segment : segments) {
            if (segment != null) array.add(segment);
        }
        return array;
    }

    public static JSONObject text(String text) {
        JSONObject data = new JSONObject(true);
        data.put("text",text == null ? "" : text);
        return segment("text",data);
    }

    public static JSONObject image(String file) {
        JSONObject data = new JSONObject(true);
        data.put("file",file == null ? "" : file);
        return segment("image",data);
    }

    public static JSONObject at(long userID) {
        JSONObject data = new JSONObject(true);
        data.put("qq",String.valueOf(userID));
        return segment("at",data);
    }

    public static JSONObject reply(long messageID) {
        JSONObject data = new JSONObject(true);
        data.put("id",String.valueOf(messageID));
        return segment("reply",data);
    }

    public static JSONObject face(int faceID) {
        JSONObject data = new JSONObject(true);
        data.put("id",String.valueOf(faceID));
        return segment("face",data);
    }

    public static JSONObject record(String file) {
        JSONObject data = new JSONObject(true);
        data.put("file",file == null ? "" : file);
        return segment("record",data);
    }

    public static JSONObject segment(String type,JSONObject data) {
        JSONObject segment = new JSONObject(true);
        segment.put("type",type);
        segment.put("data",data == null ? new JSONObject(true) : data);
        return segment;
    }
}
