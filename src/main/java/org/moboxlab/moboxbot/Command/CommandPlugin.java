package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Plugin.PluginManagerImpl;
import org.moboxlab.moboxbot.Plugin.PluginRecord;
import org.moboxlab.moboxlib.Object.ObjectCommand;
import org.moboxlab.moboxlib.Object.ObjectLogger;

import java.util.ArrayList;
import java.util.List;

public class CommandPlugin extends ObjectCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("plugin");
        prefixList.add("plugins");
        return prefixList;
    }

    @Override
    public boolean execute(String[] args,ObjectLogger logger) {
        PluginManagerImpl manager = PluginManagerImpl.get();
        if (manager == null) {
            BasicInfo.logger.sendWarn("插件内核尚未初始化！");
            return false;
        }
        if (args.length <= 1 || "list".equalsIgnoreCase(args[1])) {
            List<PluginRecord> records = manager.getRecords();
            if (records.isEmpty()) {
                BasicInfo.logger.sendInfo("当前没有插件！");
                return true;
            }
            for (PluginRecord record : records) {
                BasicInfo.logger.sendInfo(record.getName()+" | "+record.state.name()
                        +" | 监听 "+record.listeners.size()
                        +" | 命令 "+record.commands.size()
                        +" | 任务 "+record.tasks.size()
                        +" | "+record.message);
            }
            return true;
        }
        if ("info".equalsIgnoreCase(args[1]) && args.length >= 3) {
            PluginRecord record = manager.getRecord(args[2]);
            if (record == null) {
                BasicInfo.logger.sendWarn("没有找到插件："+args[2]);
                return true;
            }
            BasicInfo.logger.sendInfo("插件："+record.getName());
            BasicInfo.logger.sendInfo("版本："+record.description.version);
            BasicInfo.logger.sendInfo("API："+record.description.apiVersion);
            BasicInfo.logger.sendInfo("状态："+record.state.name());
            BasicInfo.logger.sendInfo("路径："+record.description.filePath);
            BasicInfo.logger.sendInfo("原因："+record.message);
            return true;
        }
        if ("enable".equalsIgnoreCase(args[1]) && args.length >= 3) {
            BasicInfo.logger.sendInfo(manager.enablePlugin(args[2]) ? "插件已启用："+args[2] : "插件启用失败："+args[2]);
            return true;
        }
        if ("disable".equalsIgnoreCase(args[1]) && args.length >= 3) {
            BasicInfo.logger.sendInfo(manager.disablePlugin(args[2]) ? "插件已停用："+args[2] : "插件停用失败："+args[2]);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1]) && args.length >= 3) {
            BasicInfo.logger.sendInfo(manager.reloadPlugin(args[2]) ? "插件已重载："+args[2] : "插件重载失败："+args[2]);
            return true;
        }
        BasicInfo.logger.sendInfo("用法：plugin list | plugin info <name> | plugin enable/disable/reload <name>");
        return true;
    }
}
