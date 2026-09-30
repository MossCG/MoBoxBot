package org.moboxlab.moboxbot.OneBot;

import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Task.SchedulerService;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * OneBot 连接入口
 * 默认正向 WebSocket，支持反向 WebSocket。
 */
public class OneBotMain {
    private static ForwardClient forwardClient;
    private static ReverseServer reverseServer;
    private static volatile WebSocket reverseConnection;
    private static volatile boolean connected = false;
    private static volatile boolean closing = false;
    private static volatile long lastHeartbeat = 0L;
    private static int reconnectDelay = 1000;

    public static void init() {
        BasicInfo.oneBotClient = new OneBotClientImpl();
        closing = false;
        String mode = BasicInfo.getConfigString("oneBotMode","forward-ws");
        BasicInfo.logger.sendInfo("正在初始化 OneBot 模块，模式："+mode);
        if ("reverse-ws".equalsIgnoreCase(mode)) {
            startReverse();
        } else {
            startForward();
        }
        int heartInterval = BasicInfo.getConfigInt("oneBotHeartInterval",30000);
        SchedulerService.runTaskTimer(() -> checkHeartbeat(heartInterval),10,10);
    }

    public static void shutdown() {
        closing = true;
        connected = false;
        try {
            if (forwardClient != null) forwardClient.close();
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
        try {
            if (reverseServer != null) reverseServer.stop();
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
    }

    public static boolean isConnected() {
        return connected;
    }

    public static String getStateText() {
        if (connected) return "已连接";
        return "未连接";
    }

    public static void markHeartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * 同步发送 Action，等待 echo 响应。
     */
    public static JSONObject sendAction(String action,JSONObject params) {
        if (!connected) {
            BasicInfo.logger.sendWarn("OneBot 尚未连接，Action 已跳过："+action);
            return null;
        }
        String echo = OneBotEcho.randomEcho();
        JSONObject request = new JSONObject(true);
        request.put("action",action);
        request.put("params",params == null ? new JSONObject(true) : params);
        request.put("echo",echo);

        CompletableFuture<JSONObject> future = OneBotEcho.create(echo);
        try {
            boolean sent;
            if (forwardClient != null) {
                sent = forwardClient.isOpen();
                if (sent) forwardClient.send(request.toJSONString());
            } else {
                WebSocket connection = reverseConnection;
                sent = connection != null && connection.isOpen();
                if (sent) connection.send(request.toJSONString());
            }
            if (!sent) {
                OneBotEcho.remove(echo);
                BasicInfo.logger.sendWarn("OneBot Action 发送失败："+action);
                return null;
            }
            long timeout = BasicInfo.getConfigLong("oneBotActionTimeout",10000L);
            return future.get(timeout,TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            BasicInfo.logger.sendWarn("OneBot Action 超时或异常："+action+"，原因："+e.getMessage());
            return null;
        } finally {
            OneBotEcho.remove(echo);
        }
    }

    private static void startForward() {
        String url = BasicInfo.getConfigString("oneBotUrl","ws://127.0.0.1:3001");
        String token = BasicInfo.getConfigString("oneBotToken","");
        try {
            Map<String,String> headers = new HashMap<>();
            if (token != null && !token.trim().isEmpty()) headers.put("Authorization","Bearer "+token);
            forwardClient = new ForwardClient(new URI(url),headers);
            forwardClient.setConnectionLostTimeout(30);
            forwardClient.connect();
            BasicInfo.logger.sendInfo("正在连接正向 WebSocket："+url);
        } catch (Exception e) {
            BasicInfo.logger.sendWarn("正向 WebSocket 连接失败："+e.getMessage());
            scheduleReconnect();
        }
    }

    private static void startReverse() {
        String host = BasicInfo.getConfigString("oneBotHost","127.0.0.1");
        int port = BasicInfo.getConfigInt("oneBotPort",3001);
        try {
            reverseServer = new ReverseServer(new InetSocketAddress(host,port));
            reverseServer.start();
            BasicInfo.logger.sendInfo("反向 WebSocket 服务端已启动："+host+":"+port);
        } catch (Exception e) {
            BasicInfo.logger.sendWarn("反向 WebSocket 启动失败："+e.getMessage());
        }
    }

    private static void scheduleReconnect() {
        if (closing) return;
        final int delay = reconnectDelay;
        reconnectDelay = Math.min(reconnectDelay*2,60000);
        SchedulerService.runTaskLater(OneBotMain::startForward,Math.max(1,delay/1000));
    }

    private static void checkHeartbeat(int heartInterval) {
        if (!connected || lastHeartbeat <= 0) return;
        if (System.currentTimeMillis() - lastHeartbeat <= heartInterval*3L) return;
        BasicInfo.logger.sendWarn("OneBot 心跳超时，准备重连！");
        if (forwardClient != null) forwardClient.close();
    }

    private static class ForwardClient extends WebSocketClient {
        public ForwardClient(URI serverUri,Map<String,String> httpHeaders) {
            super(serverUri,httpHeaders);
        }

        @Override
        public void onOpen(org.java_websocket.handshake.ServerHandshake handshakedata) {
            connected = true;
            reconnectDelay = 1000;
            lastHeartbeat = System.currentTimeMillis();
            BasicInfo.logger.sendInfo("正向 WebSocket 已连接！");
        }

        @Override
        public void onMessage(String message) {
            try {
                OneBotEvent.handle(JSONObject.parseObject(message));
            } catch (Exception e) {
                BasicInfo.sendException(e);
            }
        }

        @Override
        public void onClose(int code,String reason,boolean remote) {
            connected = false;
            BasicInfo.logger.sendWarn("正向 WebSocket 已断开："+code+" "+reason);
            scheduleReconnect();
        }

        @Override
        public void onError(Exception ex) {
            BasicInfo.logger.sendWarn("正向 WebSocket 异常："+ex.getMessage());
        }
    }

    private static class ReverseServer extends WebSocketServer {
        public ReverseServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn,ClientHandshake handshake) {
            String token = BasicInfo.getConfigString("oneBotToken","");
            if (token != null && !token.trim().isEmpty()) {
                String auth = handshake.getFieldValue("Authorization");
                if (!("Bearer "+token).equals(auth)) {
                    BasicInfo.logger.sendWarn("反向 WebSocket 鉴权失败，已拒绝连接！");
                    conn.close();
                    return;
                }
            }
            reverseConnection = conn;
            connected = true;
            lastHeartbeat = System.currentTimeMillis();
            BasicInfo.logger.sendInfo("反向 WebSocket 客户端已连接："+conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn,int code,String reason,boolean remote) {
            if (reverseConnection == conn) {
                reverseConnection = null;
                connected = false;
            }
            BasicInfo.logger.sendWarn("反向 WebSocket 客户端已断开："+code+" "+reason);
        }

        @Override
        public void onMessage(WebSocket conn,String message) {
            try {
                OneBotEvent.handle(JSONObject.parseObject(message));
            } catch (Exception e) {
                BasicInfo.sendException(e);
            }
        }

        @Override
        public void onError(WebSocket conn,Exception ex) {
            BasicInfo.logger.sendWarn("反向 WebSocket 异常："+ex.getMessage());
        }

        @Override
        public void onStart() {
            BasicInfo.logger.sendInfo("反向 WebSocket 服务端启动完成！");
        }
    }
}
