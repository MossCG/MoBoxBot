package org.moboxlab.moboxbot;

import org.moboxlab.moboxbot.API.Event.GroupMessageEvent;
import org.moboxlab.moboxbot.API.Command.CommandPermission;
import org.moboxlab.moboxbot.API.Command.CommandSender;
import org.moboxlab.moboxbot.Database.DatabaseMain;
import org.moboxlab.moboxbot.Database.TableInitializer;
import org.moboxlab.moboxbot.Plugin.PluginManagerImpl;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;
import org.moboxlab.moboxbot.Task.SchedulerService;
import org.moboxlab.moboxlib.Config.ConfigManager;
import org.moboxlab.moboxlib.Object.ObjectLogger;

/**
 * M0-M7 轻量测试入口
 */
public class TestMain {
    public static void main(String[] args) {
        BasicInfo.logger = new ObjectLogger(BasicInfo.runDir+"/logs");
        BasicInfo.config = ConfigManager.getConfigObject(BasicInfo.runDir,"config.yml","config.yml");
        DatabaseMain.init();
        TableInitializer.init();
        SchedulerService.start();
        PluginManagerImpl.init();

        GroupMessageEvent event = new GroupMessageEvent();
        event.setGroupID(10001L);
        event.setUserID(20002L);
        event.setRawMessage("test-event");
        event.setMessage(new com.alibaba.fastjson.JSONArray());
        event.setSender(new com.alibaba.fastjson.JSONObject(true));
        event.setRaw(new com.alibaba.fastjson.JSONObject(true));
        PluginManagerImpl.get().getEventBus().callEvent(event);

        final String[] commandReply = new String[1];
        CommandSender sender = new CommandSender() {
            @Override
            public boolean isGroup() {
                return true;
            }

            @Override
            public long getGroupID() {
                return 10001L;
            }

            @Override
            public long getUserID() {
                return 20002L;
            }

            @Override
            public String getName() {
                return "测试用户";
            }

            @Override
            public String getCommandPrefix() {
                return "/";
            }

            @Override
            public boolean hasPermission(CommandPermission permission) {
                return true;
            }

            @Override
            public void sendMessage(String message) {
                commandReply[0] = message;
            }

            @Override
            public void reply(String message) {
                commandReply[0] = message;
            }
        };
        if (!CommandRegistry.dispatch(sender,"/ping") || !"pong".equals(commandReply[0])) {
            throw new RuntimeException("聊天命令测试失败！");
        }
        BasicInfo.logger.sendInfo("聊天命令测试通过！");

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        SchedulerService.stop();
        DatabaseMain.close();
        BasicInfo.logger.sendInfo("TestMain 完成！");
    }
}
