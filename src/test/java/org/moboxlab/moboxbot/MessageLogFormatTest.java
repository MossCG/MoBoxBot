package org.moboxlab.moboxbot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.OneBot.MessageUtil;
import org.moboxlab.moboxbot.Util.MessageLogUtil;

/**
 * 消息日志格式检查
 */
public class MessageLogFormatTest {
    public static void main(String[] args) {
        JSONArray message = MessageUtil.message(
                MessageUtil.text("你好 "),
                MessageUtil.image("test.jpg"),
                MessageUtil.at(123456L),
                MessageUtil.face(1));
        String actual = MessageLogUtil.formatMessage(message);
        String expected = "你好 [图片]@123456[表情]";
        if (!expected.equals(actual)) {
            throw new IllegalStateException("消息段日志格式错误："+actual);
        }
        String raw = MessageLogUtil.formatRawMessage("看图[CQ:image,file=test.jpg]并呼叫[CQ:at,qq=123456]");
        if (!"看图[图片]并呼叫@123456".equals(raw)) {
            throw new IllegalStateException("raw_message 日志格式错误："+raw);
        }
        JSONObject heartbeat = new JSONObject(true);
        heartbeat.put("post_type","meta_event");
        heartbeat.put("meta_event_type","heartbeat");
        if (!MessageLogUtil.isHeartbeat(heartbeat)) {
            throw new IllegalStateException("heartbeat 事件过滤检查失败！");
        }
        System.out.println("消息日志格式检查通过！");
    }
}
