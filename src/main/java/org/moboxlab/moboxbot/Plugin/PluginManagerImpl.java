package org.moboxlab.moboxbot.Plugin;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Command.CommandInfo;
import org.moboxlab.moboxbot.API.Event.Listener;
import org.moboxlab.moboxbot.API.MoBoxBotAPI;
import org.moboxlab.moboxbot.API.Plugin;
import org.moboxlab.moboxbot.API.PluginDescription;
import org.moboxlab.moboxbot.API.PluginLogger;
import org.moboxlab.moboxbot.API.PluginManager;
import org.moboxlab.moboxbot.API.PluginInfo;
import org.moboxlab.moboxbot.API.PluginService;
import org.moboxlab.moboxbot.API.PluginState;
import org.moboxlab.moboxbot.API.Storage.StorageService;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Database.SqlExecutor;
import org.moboxlab.moboxbot.Plugin.Registry.CommandRegistry;
import org.moboxlab.moboxbot.Task.SchedulerService;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 插件内核
 */
public class PluginManagerImpl implements PluginManager {
    private static final String pluginDir = BasicInfo.runDir+"/plugins";
    private static PluginManagerImpl instance;

    private final EventBusImpl eventBus = new EventBusImpl();
    private final StorageServiceImpl storageService = new StorageServiceImpl();
    private final Map<String,PluginRecord> recordMap = new LinkedHashMap<>();
    private final Map<String,ServiceEntry> serviceMap = new LinkedHashMap<>();

    private static class ServiceEntry {
        private final Plugin plugin;
        private final PluginService service;

        private ServiceEntry(Plugin plugin,PluginService service) {
            this.plugin = plugin;
            this.service = service;
        }
    }

    private PluginManagerImpl() {
    }

    public static PluginManagerImpl get() {
        return instance;
    }

    public static void init() {
        instance = new PluginManagerImpl();
        MoBoxBotAPI.setServer(new PluginServer());
        org.moboxlab.moboxlib.File.FileCheck.checkDirExist(pluginDir);
        BasicInfo.logger.sendInfo("正在加载插件模块......");
        List<PluginDescription> descriptions = PluginDescriptionReader.readAll(pluginDir);
        if (descriptions.isEmpty()) {
            BasicInfo.logger.sendInfo("没有发现任何插件，跳过插件加载！");
            return;
        }
        instance.loadAll(descriptions);
    }

    public EventBusImpl getEventBus() {
        return eventBus;
    }

    public StorageService getStorage() {
        return storageService;
    }

    private void loadAll(List<PluginDescription> list) {
        Map<String,PluginDescription> descriptionMap = new LinkedHashMap<>();
        for (PluginDescription description : list) {
            if (descriptionMap.containsKey(description.name)) {
                BasicInfo.logger.sendWarn("插件名重复，只加载第一个："+description.name+"（"+description.fileName+"）");
                continue;
            }
            descriptionMap.put(description.name,description);
        }

        List<PluginDescription> pending = new ArrayList<>();
        for (PluginDescription description : descriptionMap.values()) {
            String missing = missingDepend(description,descriptionMap);
            if (missing != null) {
                markFailed(description,"缺少依赖插件："+missing);
                continue;
            }
            pending.add(description);
        }

        List<PluginDescription> sorted = new ArrayList<>();
        boolean moved = true;
        while (moved && !pending.isEmpty()) {
            moved = false;
            Iterator<PluginDescription> iterator = pending.iterator();
            while (iterator.hasNext()) {
                PluginDescription description = iterator.next();
                if (dependReady(description,sorted,descriptionMap)) {
                    sorted.add(description);
                    iterator.remove();
                    moved = true;
                }
            }
        }
        for (PluginDescription description : pending) {
            markFailed(description,"存在循环依赖，无法确定加载顺序");
        }
        for (PluginDescription description : sorted) loadOne(description);

        int enabled = 0;
        for (PluginDescription description : sorted) {
            if (enablePlugin(description.name)) enabled++;
        }
        BasicInfo.logger.sendInfo("插件加载完成：共发现 "+descriptionMap.size()+" 个，启用 "+enabled
                +" 个，失败 "+countState(PluginState.FAILED)+" 个！");
    }

    private void loadOne(PluginDescription description) {
        PluginRecord record = new PluginRecord(description);
        recordMap.put(description.name,record);
        try {
            String dataFolder = pluginDir+"/"+description.name;
            org.moboxlab.moboxlib.File.FileCheck.checkDirExist(dataFolder);
            record.dataFolder = dataFolder;
            PluginClassLoader classLoader = new PluginClassLoader(
                    new URL[]{new File(description.filePath).toURI().toURL()},
                    PluginManagerImpl.class.getClassLoader(),
                    description.name);
            record.classLoader = classLoader;
            Class<?> mainClass = classLoader.loadClass(description.main);
            Object object = mainClass.getDeclaredConstructor().newInstance();
            if (!(object instanceof Plugin)) {
                record.fail("main 类没有继承 API 里的 Plugin："+description.main);
                BasicInfo.logger.sendWarn("插件 "+description.name+" 加载失败："+record.message);
                return;
            }
            Plugin plugin = (Plugin) object;
            plugin.initPlugin(MoBoxBotAPI.getServer(),description,new PluginLogger(description.name,BasicInfo.logger),dataFolder);
            record.plugin = plugin;
            record.state = PluginState.LOADED;
            record.message = "已加载，等待启用";
            plugin.onLoad();
            saveRecord(record);
            BasicInfo.logger.sendInfo("已加载插件："+description.name+" "+description.version);
        } catch (Throwable throwable) {
            record.fail("加载失败："+throwable);
            BasicInfo.logger.sendWarn("插件 "+description.name+" 加载失败："+throwable);
            BasicInfo.sendException(throwable);
            closeClassLoader(record);
        }
    }

    @Override
    public Plugin getPlugin(String name) {
        PluginRecord record = recordMap.get(name);
        return record == null ? null : record.plugin;
    }

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> list = new ArrayList<>();
        for (PluginRecord record : recordMap.values()) {
            if (record.plugin != null) list.add(record.plugin);
        }
        return list;
    }

    public List<PluginRecord> getRecords() {
        return new ArrayList<>(recordMap.values());
    }

    public PluginRecord getRecord(String name) {
        return recordMap.get(name);
    }

    @Override
    public boolean isEnabled(String name) {
        PluginRecord record = recordMap.get(name);
        return record != null && record.state == PluginState.ENABLED;
    }

    @Override
    public boolean enablePlugin(String name) {
        PluginRecord record = recordMap.get(name);
        if (record == null || record.plugin == null) return false;
        if (record.state == PluginState.ENABLED) return true;
        try {
            record.plugin.setEnabled(true);
            record.plugin.onEnable();
            record.state = PluginState.ENABLED;
            record.message = "启用正常";
            record.loadTime = System.currentTimeMillis();
            saveRecord(record);
            BasicInfo.logger.sendInfo("插件已启用："+name+" "+record.description.version);
            return true;
        } catch (Throwable throwable) {
            record.plugin.setEnabled(false);
            record.fail("启用失败："+throwable);
            BasicInfo.logger.sendWarn("插件 "+name+" 启用失败："+throwable);
            BasicInfo.sendException(throwable);
            recycle(record);
            return false;
        }
    }

    @Override
    public boolean disablePlugin(String name) {
        PluginRecord record = recordMap.get(name);
        if (record == null || record.plugin == null) return false;
        if (record.state == PluginState.DISABLED) return true;
        try {
            try {
                record.plugin.onDisable();
            } catch (Throwable throwable) {
                BasicInfo.logger.sendWarn("插件 "+name+" 停用时抛出异常："+throwable);
                BasicInfo.sendException(throwable);
            }
            recycle(record);
            record.plugin.setEnabled(false);
            record.state = PluginState.DISABLED;
            record.message = "已停用";
            saveRecord(record);
            BasicInfo.logger.sendInfo("插件已停用："+name);
            return true;
        } catch (Throwable throwable) {
            record.state = PluginState.FAILED;
            record.message = "停用失败："+throwable;
            BasicInfo.logger.sendWarn("插件 "+name+" 停用流程失败："+throwable);
            BasicInfo.sendException(throwable);
            return false;
        }
    }

    @Override
    public boolean reloadPlugin(String name) {
        PluginRecord record = recordMap.get(name);
        if (record == null) return false;
        try {
            PluginDescription description = record.description;
            disablePlugin(name);
            closeClassLoader(record);
            recordMap.remove(name);
            BasicInfo.logger.sendInfo("正在重载插件："+name);
            loadOne(description);
            PluginRecord reloaded = recordMap.get(name);
            if (reloaded == null || reloaded.plugin == null) return false;
            return enablePlugin(name);
        } catch (Throwable throwable) {
            BasicInfo.logger.sendWarn("插件 "+name+" 重载失败："+throwable);
            BasicInfo.sendException(throwable);
            return false;
        }
    }

    @Override
    public void registerListener(Plugin plugin,Listener listener) {
        PluginRecord record = recordOf(plugin);
        if (record == null || listener == null) return;
        record.listeners.add(listener);
        eventBus.register(plugin,listener);
    }

    @Override
    public void registerCommand(Plugin plugin,BotCommand command) {
        PluginRecord record = recordOf(plugin);
        if (record == null || command == null) return;
        record.commands.add(command);
        CommandRegistry.register(plugin,command);
    }

    @Override
    public boolean registerService(Plugin plugin,PluginService service) {
        PluginRecord record = recordOf(plugin);
        if (record == null || service == null) return false;
        String name = service.getName();
        if (name == null || name.trim().isEmpty()) {
            BasicInfo.logger.sendWarn("插件 "+record.getName()+" 注册的服务名为空，已忽略！");
            return false;
        }
        name = name.trim();
        ServiceEntry exists = serviceMap.get(name);
        if (exists != null && exists.plugin != plugin) {
            BasicInfo.logger.sendWarn("插件服务名冲突："+name+"（已被 "+exists.plugin.getName()+" 注册）");
            return false;
        }
        serviceMap.put(name,new ServiceEntry(plugin,service));
        BasicInfo.logger.sendInfo("插件 "+record.getName()+" 注册了服务："+name);
        return true;
    }

    @Override
    public PluginService getService(String name) {
        if (name == null) return null;
        ServiceEntry entry = serviceMap.get(name.trim());
        return entry == null ? null : entry.service;
    }

    @Override
    public void runTask(Plugin plugin,Runnable task) {
        PluginRecord record = recordOf(plugin);
        if (record == null || task == null) return;
        addTask(record,SchedulerService.runTaskAsync(task));
    }

    @Override
    public void runTaskLater(Plugin plugin,Runnable task,long delaySeconds) {
        PluginRecord record = recordOf(plugin);
        if (record == null || task == null) return;
        addTask(record,SchedulerService.runTaskLater(task,delaySeconds));
    }

    @Override
    public void runTaskTimer(Plugin plugin,Runnable task,long delaySeconds,long periodSeconds) {
        PluginRecord record = recordOf(plugin);
        if (record == null || task == null) return;
        addTask(record,SchedulerService.runTaskTimer(task,delaySeconds,periodSeconds));
    }

    private void addTask(PluginRecord record,Future<?> future) {
        if (future == null) {
            BasicInfo.logger.sendWarn("插件 "+record.getName()+" 的任务没有登记成功：定时任务框架尚未启动！");
            return;
        }
        record.tasks.add(future);
    }

    public int listenerCount(String name) {
        PluginRecord record = recordMap.get(name);
        if (record == null || record.plugin == null) return 0;
        return eventBus.count(record.plugin);
    }

    public List<CommandInfo> getCommandList() {
        return CommandRegistry.getCommandInfoList();
    }

    public List<PluginInfo> getPluginInfoList() {
        List<PluginInfo> list = new ArrayList<>();
        for (PluginRecord record : recordMap.values()) {
            PluginInfo info = new PluginInfo();
            info.name = record.getName();
            info.version = record.description == null ? "" : record.description.version;
            info.author = record.description == null ? "" : record.description.author;
            info.description = record.description == null ? "" : record.description.description;
            info.enabled = record.state == PluginState.ENABLED;
            info.listenerCount = record.plugin == null ? 0 : eventBus.count(record.plugin);
            info.commandCount = record.commands.size();
            info.taskCount = record.tasks.size();
            list.add(info);
        }
        return list;
    }

    private void recycle(PluginRecord record) {
        removeServices(record.plugin);
        try {
            if (record.plugin != null) eventBus.unregisterAll(record.plugin);
        } catch (Throwable throwable) {
            BasicInfo.logger.sendWarn("注销插件监听器失败："+throwable);
        }
        try {
            CommandRegistry.unregister(record.plugin);
        } catch (Throwable throwable) {
            BasicInfo.logger.sendWarn("注销插件命令失败："+throwable);
        }
        for (Future<?> future : record.tasks) {
            try {
                future.cancel(false);
            } catch (Throwable throwable) {
                BasicInfo.logger.sendWarn("取消插件任务失败："+throwable);
            }
        }
        record.tasks.clear();
        record.listeners.clear();
        record.commands.clear();
    }

    private void removeServices(Plugin plugin) {
        if (plugin == null) return;
        List<String> removeKeys = new ArrayList<>();
        for (Map.Entry<String,ServiceEntry> entry : serviceMap.entrySet()) {
            if (entry.getValue().plugin == plugin) removeKeys.add(entry.getKey());
        }
        for (String key : removeKeys) serviceMap.remove(key);
    }

    private PluginRecord recordOf(Plugin plugin) {
        if (plugin == null) return null;
        PluginRecord record = recordMap.get(plugin.getName());
        if (record == null) BasicInfo.logger.sendWarn("插件 "+plugin.getName()+" 不在内核记录里，这次注册已忽略！");
        return record;
    }

    private void markFailed(PluginDescription description,String reason) {
        PluginRecord record = new PluginRecord(description);
        record.state = PluginState.FAILED;
        record.message = reason;
        recordMap.put(description.name,record);
        saveRecord(record);
        BasicInfo.logger.sendWarn("插件 "+description.name+" 未加载："+reason);
    }

    private String missingDepend(PluginDescription description,Map<String,PluginDescription> map) {
        for (String depend : description.depend) {
            if (!map.containsKey(depend)) return depend;
        }
        return null;
    }

    private boolean dependReady(PluginDescription description,List<PluginDescription> loaded,Map<String,PluginDescription> map) {
        List<String> loadedNames = new ArrayList<>();
        for (PluginDescription item : loaded) loadedNames.add(item.name);
        for (String depend : description.depend) {
            if (!loadedNames.contains(depend)) return false;
        }
        for (String softDepend : description.softDepend) {
            if (map.containsKey(softDepend) && !loadedNames.contains(softDepend)) return false;
        }
        for (PluginDescription other : map.values()) {
            if (other == description) continue;
            if (!other.loadBefore.contains(description.name)) continue;
            if (!loadedNames.contains(other.name)) return false;
        }
        return true;
    }

    private int countState(PluginState state) {
        int count = 0;
        for (PluginRecord record : recordMap.values()) {
            if (record.state == state) count++;
        }
        return count;
    }

    private void closeClassLoader(PluginRecord record) {
        try {
            if (record.classLoader != null) record.classLoader.close();
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
    }

    public void saveRecord(PluginRecord record) {
        if (record == null || record.description == null) return;
        try {
            String name = record.getName();
            String version = record.description.version == null ? "" : record.description.version;
            String apiVersion = record.description.apiVersion == null ? "" : record.description.apiVersion;
            String state = record.state.name();
            String message = record.message == null ? "" : record.message;
            if (message.length() > 255) message = message.substring(0,255);
            JSONObject exists = SqlExecutor.queryOne("SELECT `ID` FROM `bot_plugin_record` WHERE `name`=?",name);
            long now = System.currentTimeMillis();
            if (exists == null) {
                SqlExecutor.insert("INSERT INTO `bot_plugin_record` (`name`,`version`,`apiVersion`,`state`,`loadTime`,`message`,`fileName`,`filePath`,`dataFolder`,`updateTime`) VALUES (?,?,?,?,?,?,?,?,?,?)",
                        name,version,apiVersion,state,record.loadTime,message,
                        record.description.fileName,record.description.filePath,record.dataFolder,now);
            } else {
                SqlExecutor.update("UPDATE `bot_plugin_record` SET `version`=?,`apiVersion`=?,`state`=?,`loadTime`=?,`message`=?,`fileName`=?,`filePath`=?,`dataFolder`=?,`updateTime`=? WHERE `name`=?",
                        version,apiVersion,state,record.loadTime,message,
                        record.description.fileName,record.description.filePath,record.dataFolder,now,name);
            }
        } catch (Exception e) {
            BasicInfo.logger.sendWarn("写插件加载记录失败："+e.getMessage());
        }
    }
}
