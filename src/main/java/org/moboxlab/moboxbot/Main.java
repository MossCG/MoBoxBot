package org.moboxlab.moboxbot;

import org.moboxlab.moboxbot.Command.CommandDebug;
import org.moboxlab.moboxbot.Command.CommandExit;
import org.moboxlab.moboxbot.Command.CommandPlugin;
import org.moboxlab.moboxbot.Command.CommandReload;
import org.moboxlab.moboxbot.Command.CommandStatus;
import org.moboxlab.moboxbot.Command.MuteCommand;
import org.moboxlab.moboxbot.Database.DatabaseMain;
import org.moboxlab.moboxbot.Database.TableInitializer;
import org.moboxlab.moboxbot.OneBot.OneBotMain;
import org.moboxlab.moboxbot.Plugin.PluginManagerImpl;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;
import org.moboxlab.moboxbot.Task.SchedulerService;
import org.moboxlab.moboxlib.Command.CommandManager;
import org.moboxlab.moboxlib.Config.ConfigManager;
import org.moboxlab.moboxlib.File.FileCheck;
import org.moboxlab.moboxlib.Object.ObjectLogger;

/**
 * MoBoxBot 主程序
 */
public class Main {
    private static volatile boolean shuttingDown = false;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        BasicInfo.startTime = startTime;

        //运行目录检查
        FileCheck.checkDirExist(BasicInfo.runDir);
        FileCheck.checkDirExist(BasicInfo.runDir+"/data");
        FileCheck.checkDirExist(BasicInfo.runDir+"/logs");
        FileCheck.checkDirExist(BasicInfo.runDir+"/plugins");
        FileCheck.checkDirExist(BasicInfo.runDir+"/dependency");

        //日志模块初始化
        ObjectLogger logger = new ObjectLogger(BasicInfo.runDir+"/logs");
        BasicInfo.logger = logger;

        //基础信息输出
        logger.sendInfo("欢迎使用 MoBoxBot QQ 机器人！");
        logger.sendInfo("软件版本："+BasicInfo.version);
        logger.sendInfo("软件作者："+BasicInfo.author);

        //配置读取
        logger.sendInfo("正在读取配置文件......");
        BasicInfo.config = ConfigManager.getConfigObject(BasicInfo.runDir,"config.yml","config.yml");
        if (BasicInfo.config == null) {
            logger.sendWarn("配置文件读取失败，请检查 config.yml！");
            System.exit(0);
        }
        if (!BasicInfo.getConfigBoolean("enable",false)) {
            logger.sendInfo("你还没有完成配置文件的设置哦~");
            logger.sendInfo("配置文件位置："+BasicInfo.runDir+"/config.yml");
            System.exit(0);
        }
        BasicInfo.debug = BasicInfo.getConfigBoolean("debug",false);

        //数据库初始化
        logger.sendInfo("正在初始化数据库模块......");
        DatabaseMain.init();
        TableInitializer.init();

        //定时任务框架
        SchedulerService.start();

        //插件加载（必须在 OneBot 连接前）
        PluginManagerImpl.init();

        //内置聊天命令（仅登记元数据，实际闭麦拦截在 OneBotEvent）
        CommandRegistry.register(new MuteCommand());

        //OneBot 连接
        OneBotMain.init();

        //控制台命令
        CommandManager.initCommand(BasicInfo.logger,true);
        CommandManager.registerCommand(new CommandStatus());
        CommandManager.registerCommand(new CommandPlugin());
        CommandManager.registerCommand(new CommandReload());
        CommandManager.registerCommand(new CommandDebug());
        CommandManager.registerCommand(new CommandExit());

        Runtime.getRuntime().addShutdownHook(new Thread(Main::cleanup));

        long completeTime = System.currentTimeMillis();
        logger.sendInfo("启动完成！耗时："+(completeTime-startTime)+"毫秒！");
    }

    /**
     * 热重载配置
     */
    public static void reloadConfig() {
        BasicInfo.logger.sendInfo("正在重载配置文件......");
        BasicInfo.config = ConfigManager.getConfigObject(BasicInfo.runDir,"config.yml","config.yml");
        BasicInfo.debug = BasicInfo.getConfigBoolean("debug",false);
        BasicInfo.logger.sendInfo("重载完成！");
    }

    /**
     * 控制台 exit / stop 调用
     */
    public static void shutdown() {
        if (shuttingDown) return;
        shuttingDown = true;
        cleanup();
        System.exit(0);
    }

    /**
     * 统一收尾，shutdown hook 也会调用
     */
    private static void cleanup() {
        if (BasicInfo.logger == null) return;
        BasicInfo.logger.sendInfo("正在关闭 MoBoxBot......");
        try {
            OneBotMain.shutdown();
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
        try {
            SchedulerService.stop();
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
        try {
            DatabaseMain.close();
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
        BasicInfo.logger.sendInfo("MoBoxBot 已退出！");
    }
}
