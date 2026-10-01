# API.md — MoBoxBot 插件 API 草案

> 状态：M0 草案 v0.1
> 包名：`org.moboxlab.moboxbot.API`
> 适用：MoBoxBot 插件开发。
> 说明：本文定义 API 形状，M1 实现时可以调整实现细节，但破坏性变更必须进位 `API_VERSION`。

## 1. 设计原则

- 插件只允许依赖 `org.moboxlab.moboxbot.API`。
- API 包不依赖主程序内部实现。
- 主程序内部包不得出现在插件公开签名里。
- 插件拿到的所有服务都从 `Server` 门面走。
- 事件与消息继续使用 fastjson 的 `JSONObject` / `JSONArray`，不额外造复杂对象树。
- 所有注册资源必须能按插件回收。

## 2. 包结构

```text
org.moboxlab.moboxbot.API
├─ MoBoxBotAPI.java
├─ Server.java
├─ Plugin.java
├─ PluginDescription.java
├─ PluginState.java
├─ PluginManager.java
├─ PluginLogger.java
├─ Command/
│  ├─ BotCommand.java
│  ├─ CommandInfo.java
│  ├─ CommandSender.java
│  └─ CommandPermission.java
├─ Event/
│  ├─ Event.java
│  ├─ Listener.java
│  ├─ EventHandler.java
│  ├─ EventPriority.java
│  ├─ EventBus.java
│  ├─ MessageEvent.java
│  ├─ GroupMessageEvent.java
│  ├─ PrivateMessageEvent.java
│  ├─ NoticeEvent.java
│  ├─ RequestEvent.java
│  ├─ MetaEvent.java
│  └─ RawEvent.java
├─ OneBot/
│  ├─ OneBotClient.java
│  └─ MessageUtil.java
├─ Storage/
│  └─ StorageService.java
└─ Util/
   ├─ ImageUtil.java
   ├─ PluginConfig.java
   └─ Text.java
```

## 3. 核心入口

### 3.1 MoBoxBotAPI

```java
package org.moboxlab.moboxbot.API;

/**
 * 插件 API 静态入口
 * 插件只认这个包，不要碰主程序内部实现。
 */
public class MoBoxBotAPI {
    public static final String API_VERSION = "0.1";

    private static Server server;

    public static String getVersion() {
        return server == null ? "" : server.getVersion();
    }

    public static String getApiVersion() {
        return API_VERSION;
    }

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server pluginServer) {
        server = pluginServer;
    }
}
```

### 3.2 Server

```java
package org.moboxlab.moboxbot.API;

import org.moboxlab.moboxbot.API.OneBot.OneBotClient;
import org.moboxlab.moboxbot.API.Storage.StorageService;

/**
 * 主程序服务门面
 * 插件从 getServer() 拿这一切，不要直接访问 BasicInfo。
 */
public interface Server {
    String getVersion();

    String getApiVersion();

    String getBotName();

    PluginManager getPluginManager();

    EventBus getEventBus();

    OneBotClient getOneBotClient();

    StorageService getStorage();

    void reloadConfig();

    List<CommandInfo> getCommandList();

    List<Long> getAdminList();

    boolean addAdmin(long userID);

    boolean removeAdmin(long userID);
}
```

## 4. 插件基础

### 4.1 Plugin

```java
package org.moboxlab.moboxbot.API;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 插件基类
 * 生命周期：onLoad → onEnable → onDisable
 */
public abstract class Plugin {
    private PluginDescription description;
    private PluginLogger logger;
    private Server server;
    private String dataFolder;
    private boolean enabled = false;

    public void initPlugin(Server pluginServer,PluginDescription pluginDescription,PluginLogger pluginLogger,String pluginDataFolder) {
        //由主程序装配，插件不要调用
    }

    public String getName() {
        return description == null ? "" : description.name;
    }

    public String getVersion() {
        return description == null ? "" : description.version;
    }

    public PluginDescription getDescription() {
        return description;
    }

    public PluginLogger getLogger() {
        return logger;
    }

    public Server getServer() {
        return server;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public boolean saveDefaultConfig() {
        //首次调用时从插件 JAR 释放 config.yml 到数据目录
        return true;
    }

    public PluginConfig getConfig() {
        //插件配置，键值对 YAML 子集
        return null;
    }

    public byte[] readResource(String entryName) {
        //读插件自己 JAR 里的资源
        return null;
    }

    public String readResourceText(String entryName) {
        //读插件自己 JAR 里的 UTF-8 文本
        return "";
    }

    public void onLoad() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
```

### 4.2 PluginDescription

```java
package org.moboxlab.moboxbot.API;

import java.util.ArrayList;
import java.util.List;

/**
 * plugin.json 的解析结果
 */
public class PluginDescription {
    public String name = "";
    public String version = "";
    public String apiVersion = "";
    public String main = "";
    public String author = "";
    public String description = "";
    public String website = "";
    public List<String> depend = new ArrayList<>();
    public List<String> softDepend = new ArrayList<>();
    public List<String> loadBefore = new ArrayList<>();
    public List<String> provides = new ArrayList<>();
    public String filePath = "";
    public String fileName = "";

    public boolean isComplete() {
        return name != null && !name.trim().isEmpty()
                && main != null && !main.trim().isEmpty();
    }
}
```

### 4.3 PluginState

```java
package org.moboxlab.moboxbot.API;

public enum PluginState {
    LOADED,
    ENABLED,
    DISABLED,
    FAILED
}
```

### 4.4 PluginLogger

```java
package org.moboxlab.moboxbot.API;

/**
 * 插件日志
 * 输出自动带 [插件名] 前缀。
 */
public class PluginLogger {
    public void sendInfo(String message) {
    }

    public void sendWarn(String message) {
    }

    public void sendError(String message) {
    }

    public void sendException(Exception exception) {
    }
}
```

## 5. 插件管理器

```java
package org.moboxlab.moboxbot.API;

import org.moboxlab.moboxbot.API.Command.BotCommand;
import org.moboxlab.moboxbot.API.Event.Listener;

import java.util.List;

/**
 * 插件管理器
 * 每个注册方法都带插件自己，主程序按插件记账并回收。
 */
public interface PluginManager {
    Plugin getPlugin(String name);

    List<Plugin> getPlugins();

    boolean isEnabled(String name);

    void registerListener(Plugin plugin,Listener listener);

    void registerCommand(Plugin plugin,BotCommand command);

    void runTask(Plugin plugin,Runnable task);

    void runTaskLater(Plugin plugin,Runnable task,long delaySeconds);

    void runTaskTimer(Plugin plugin,Runnable task,long delaySeconds,long periodSeconds);

    boolean enablePlugin(String name);

    boolean disablePlugin(String name);

    boolean reloadPlugin(String name);
}
```

## 6. 事件

### 6.1 基础接口

```java
package org.moboxlab.moboxbot.API.Event;

public interface Listener {
}
```

```java
package org.moboxlab.moboxbot.API.Event;

/**
 * 所有事件的基类
 */
public abstract class Event {
    private boolean cancelled = false;

    public abstract String getName();

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
```

```java
package org.moboxlab.moboxbot.API.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    EventPriority priority() default EventPriority.NORMAL;

    boolean ignoreCancelled() default true;
}
```

```java
package org.moboxlab.moboxbot.API.Event;

/**
 * MONITOR 只用于观察与记录，不要在里面改事件或业务数据。
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR
}
```

```java
package org.moboxlab.moboxbot.API.Event;

public interface EventBus {
    void register(Plugin plugin,Listener listener);

    void unregister(Plugin plugin,Listener listener);

    void unregisterAll(Plugin plugin);

    Event callEvent(Event event);

    void callEventAsync(Event event);
}
```

### 6.2 事件类草案

| 事件类 | 对应 OneBot 事件 |
|---|---|
| `GroupMessageEvent` | `message.group` |
| `PrivateMessageEvent` | `message.private` |
| `NoticeEvent` | `notice.*` |
| `RequestEvent` | `request.*` |
| `MetaEvent` | `meta_event.*` |
| `RawEvent` | 未识别事件的兜底 |

消息事件字段：

```java
package org.moboxlab.moboxbot.API.Event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GroupMessageEvent extends Event {
    private long groupID;
    private long userID;
    private long messageID;
    private String rawMessage;
    private JSONArray message;
    private JSONObject sender;
    private JSONObject raw;

    public String getName() {
        return "GroupMessageEvent";
    }

    public long getGroupID() {
        return groupID;
    }

    public long getUserID() {
        return userID;
    }

    public long getMessageID() {
        return messageID;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public JSONArray getMessage() {
        return message;
    }

    public JSONObject getSender() {
        return sender;
    }

    public JSONObject getRaw() {
        return raw;
    }
}
```

`PrivateMessageEvent` 字段与群消息类似，但核心是 `userID`。

## 7. 聊天命令

### 7.1 CommandPermission

```java
package org.moboxlab.moboxbot.API.Command;

public enum CommandPermission {
    OWNER,
    BOT_ADMIN,
    GROUP_OWNER,
    GROUP_ADMIN,
    EVERYONE
}
```

### 7.2 CommandSender

```java
package org.moboxlab.moboxbot.API.Command;

public interface CommandSender {
    boolean isGroup();

    long getGroupID();

    long getUserID();

    String getName();

    String getCommandPrefix();

    boolean hasPermission(CommandPermission permission);

    void sendMessage(String message);

    void reply(String message);

    default void sendImage(String file) {
        //发送图片消息，file 支持 base64://、file:// 或 http://
    }
}
```

### 7.3 BotCommand

```java
package org.moboxlab.moboxbot.API.Command;

import java.util.List;

/**
 * 聊天命令
 * 注册后由命令路由器匹配前缀、权限与冷却。
 */
public abstract class BotCommand {
    public abstract List<String> prefix();

    public CommandPermission permission() {
        return CommandPermission.EVERYONE;
    }

    public int cooldownSeconds() {
        return 0;
    }

    public String description() {
        return "";
    }

    public abstract boolean execute(CommandSender sender,String[] args);
}
```

## 8. OneBot 客户端

```java
package org.moboxlab.moboxbot.API.OneBot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * OneBot 客户端
 * 所有主动调用统一走这里。
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
```

### 8.1 MessageUtil

```java
package org.moboxlab.moboxbot.API.OneBot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * OneBot 消息段构造工具
 */
public class MessageUtil {
    public static JSONArray message(JSONObject... segments) {
        return null;
    }

    public static JSONObject text(String text) {
        return null;
    }

    public static JSONObject image(String file) {
        return null;
    }

    public static JSONObject at(long userID) {
        return null;
    }

    public static JSONObject reply(long messageID) {
        return null;
    }

    public static JSONObject face(int faceID) {
        return null;
    }

    public static JSONObject record(String file) {
        return null;
    }
}
```

### 8.2 ImageUtil

```java
package org.moboxlab.moboxbot.API.Util;

import java.util.List;

/**
 * 插件图片渲染工具
 */
public class ImageUtil {
    public static byte[] renderText(String title,List<String> lines) {
        //把标题和文本行渲染成 PNG
        return null;
    }

    public static String toBase64Uri(byte[] imageBytes) {
        //转换成 OneBot 图片消息可用的 base64:// 地址
        return "";
    }
}
```

## 9. 存储

```java
package org.moboxlab.moboxbot.API.Storage;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface StorageService {
    String get(Plugin plugin,String key);

    void set(Plugin plugin,String key,String value);

    void remove(Plugin plugin,String key);

    List<JSONObject> query(String sql,Object... params);

    JSONObject queryOne(String sql,Object... params);

    int update(String sql,Object... params);

    long insert(String sql,Object... params);
}
```

规则：

- 插件自定义表必须以 `plugin_<插件名>_` 开头。
- `StorageService` 会校验表名前缀，防止插件误写主程序表。
- 所有 SQL 走主程序 `SqlExecutor`，插件不持有 JDBC 连接。

## 10. 线程与异步

- 事件默认同步分发。
- 插件耗时操作必须用 `PluginManager.runTask` / `runTaskLater` / `runTaskTimer`。
- `OneBotClient` 的调用是同步等待响应，插件不要在事件线程里做长时间阻塞。
- 发送类 Action 由主程序统一排队和限速。
- 插件自己创建的线程必须在 `onDisable` 中停止。

## 11. 错误约定

- 插件加载失败：状态 `FAILED`，原因写日志与 `bot_plugin_record`。
- 插件启用失败：已注册资源自动回收。
- API 调用失败：返回 `JSONObject` 保留 `status`、`retcode`、`data`，不抛异常给插件。
- 事件处理异常：只记录日志，不影响其他监听器。

## 12. 版本与兼容

| 版本 | 说明 |
|---|---|
| `0.1` | M0 草案：插件基类、事件总线、命令、配置、OneBot 客户端、存储接口 |

规则：

- `plugin.json` 的 `apiVersion` 主版本必须与主程序一致。
- 破坏性变更进位 API 次版本。
- 非破坏性新增保留旧插件兼容。
- 老插件重新编译前，不得依赖新增接口。

## 13. M1 实现清单

1. 建立 `API` 包全部接口与基类。
2. 建立 `PluginManagerImpl`、`PluginClassLoader`、`PluginDescriptionReader`、`PluginRecord`。
3. 建立 `EventBusImpl` 与事件注册 / 分发。
4. 建立 `PluginServer` 门面与 `MoBoxBotAPI` 静态入口。
5. 建立命令注册表与权限校验。
6. 建立 `OneBotClient` 门面，先接正向 WebSocket。
7. 建立 `StorageService` 与 SQLite 适配。

## 14. 参考

- [PLUGIN.md](PLUGIN.md)：插件开发规范。
- [ONEBOT.md](ONEBOT.md)：OneBot 11 覆盖清单。
- `D:\CodeX\Projects\MoBoxPanel\API.md`：MoBoxPanel 接口契约参考。
- `_ref/NapCatDocs/src/onebot/`：NapCat OneBot 11 文档。
