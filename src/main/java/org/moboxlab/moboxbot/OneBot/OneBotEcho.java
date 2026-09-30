package org.moboxlab.moboxbot.OneBot;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * echo 响应等待表
 */
public class OneBotEcho {
    private static final Map<String,CompletableFuture<JSONObject>> waiting = new ConcurrentHashMap<>();

    public static CompletableFuture<JSONObject> create(String echo) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        waiting.put(echo,future);
        return future;
    }

    public static String randomEcho() {
        return UUID.randomUUID().toString();
    }

    public static void complete(JSONObject response) {
        if (response == null) return;
        String echo = response.getString("echo");
        if (echo == null || echo.isEmpty()) return;
        CompletableFuture<JSONObject> future = waiting.remove(echo);
        if (future != null) future.complete(response);
    }

    public static void remove(String echo) {
        if (echo == null) return;
        waiting.remove(echo);
    }
}
