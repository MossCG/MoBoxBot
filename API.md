# API.md — MoBoxBot 插件 API 0.3（冻结）

> 状态：冻结
> 冻结日期：2026-10-02
> API 版本：`0.3`
> 包名：`org.moboxlab.moboxbot.API`
> 官方示例：https://github.com/MossCG/MBB-ExamplePlugin

## 1. 冻结范围

插件只能依赖 `org.moboxlab.moboxbot.API`。

本文件列出的类、接口、方法、字段和 JSON 数据结构属于 API 0.3 的公开契约。

以下变化属于破坏性变更：

- 删除或重命名公开类、接口、方法、字段或枚举值。
- 修改公开方法参数、返回类型或字段类型。
- 修改已公开事件字段的含义。
- 修改命令权限、生命周期或资源回收语义。
- 修改 `plugin.json` 必填字段或加载规则。

破坏性变更必须进位 API 次版本，例如 `0.1` 到 `0.2`，并同步更新：

- `MoBoxBotAPI.API_VERSION`
- [API.md](API.md)
- [PLUGIN.md](PLUGIN.md)
- 官方示例插件

## 2. 包结构

```text
org.moboxlab.moboxbot.API
├─ MoBoxBotAPI.java
├─ Server.java
├─ Plugin.java
├─ PluginDescription.java
├─ PluginInfo.java
├─ PluginManager.java
├─ PluginLogger.java
├─ PluginService.java
├─ PluginState.java
├─ Command/
│  ├─ BotCommand.java
│  ├─ CommandInfo.java
│  ├─ CommandPermission.java
│  └─ CommandSender.java
├─ Event/
│  ├─ Event.java
│  ├─ EventBus.java
│  ├─ EventHandler.java
│  ├─ EventPriority.java
│  ├─ Listener.java
│  ├─ MessageEvent.java
│  ├─ GroupMessageEvent.java
│  ├─ PrivateMessageEvent.java
│  ├─ NoticeEvent.java
│  ├─ RequestEvent.java
│  ├─ MetaEvent.java
│  └─ RawEvent.java
├─ OneBot/
│  ├─ MessageUtil.java
│  └─ OneBotClient.java
├─ Storage/
│  └─ StorageService.java
└─ Util/
   ├─ ImageUtil.java
   ├─ PluginConfig.java
   └─ Text.java
```

## 3. 最小插件

```java
package org.moboxlab.mbb.example;

import org.moboxlab.moboxbot.API.Plugin;

public class ExamplePlugin extends Plugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerCommand(this,new ExampleCommand());
        getLogger().sendInfo("示例插件已启用！");
    }
}
```

`plugin.json`：

```json
{
  "name": "MBB-ExamplePlugin",
  "version": "V0.1.1.0.0210",
  "apiVersion": "0.3",
  "main": "org.moboxlab.mbb.example.ExamplePlugin",
  "author": "MoBoxLab",
  "description": "MoBoxBot 官方插件开发示例",
  "website": "https://github.com/MossCG/MBB-ExamplePlugin",
  "depend": [],
  "softDepend": [],
  "loadBefore": [],
  "provides": ["example"]
}
```

## 4. 静态入口

### MoBoxBotAPI

| 成员 | 说明 |
|---|---|
| `API_VERSION` | 当前 API 版本，固定为 `0.3` |
| `getVersion()` | 主程序版本 |
| `getApiVersion()` | API 版本 |
| `getServer()` | 主程序服务门面 |
| `setServer(Server)` | 仅供主程序装配使用，插件不要调用 |

插件推荐通过 `Plugin` 基类里的 `getServer()` 访问。

## 5. 服务门面

`Server` 是插件访问主程序能力的唯一稳定入口。

| 方法 | 说明 |
|---|---|
| `getVersion()` | 主程序版本 |
| `getApiVersion()` | API 版本 |
| `getBotName()` | 配置中的机器人名称 |
| `getPluginManager()` | 插件管理器 |
| `getEventBus()` | 事件总线，通常不需要直接使用 |
| `getOneBotClient()` | OneBot 客户端 |
| `getStorage()` | 插件存储 |
| `reloadConfig()` | 重载主程序配置 |
| `getCommandList()` | 获取已注册命令元数据 |
| `getAdminList()` | 获取机器人管理员 QQ 列表 |
| `getOwnerList()` | 获取机器人所有者 QQ 列表 |
| `addAdmin(long)` | 添加机器人管理员 |
| `removeAdmin(long)` | 移除机器人管理员 |
| `getPluginInfoList()` | 获取插件展示信息 |

`addAdmin` 和 `removeAdmin` 会写回 `config.yml` 并触发热重载。

## 6. 插件基类

### 生命周期

```text
读取 plugin.json
  → 创建独立类加载器
  → 实例化主类
  → initPlugin
  → onLoad
  → onEnable
  → onDisable
```

规则：

- `onLoad` 只读取配置与资源，不访问其他插件。
- `onEnable` 注册监听器、命令和任务。
- `onDisable` 只释放插件自己创建的资源。
- 主程序会统一回收插件登记的监听器、命令和任务。
- 插件自己创建的线程、连接池、临时文件必须自行关闭。

### 可用方法

| 方法 | 说明 |
|---|---|
| `getName()` | 插件名 |
| `getVersion()` | 插件版本 |
| `getDescription()` | `plugin.json` 解析结果 |
| `getLogger()` | 插件日志 |
| `getServer()` | 服务门面 |
| `isEnabled()` | 是否已启用 |
| `getDataFolder()` | 插件数据目录 |
| `getConfig()` | 插件配置对象 |
| `saveDefaultConfig()` | 首次运行时释放 JAR 内 `config.yml` |
| `readResource(String)` | 读取 JAR 内资源字节 |
| `readResourceText(String)` | 读取 JAR 内 UTF-8 文本 |

`initPlugin` 与 `setEnabled` 仅供主程序调用，插件不要覆盖或主动调用。

### PluginDescription

`plugin.json` 的解析结果，公开字段：

| 字段 | 说明 |
|---|---|
| `name` | 插件唯一名称 |
| `version` | 插件版本 |
| `apiVersion` | 依赖的 API 版本 |
| `main` | 主类全限定名 |
| `author` | 作者 |
| `description` | 描述 |
| `website` | 网站 |
| `depend` | 强依赖插件列表 |
| `softDepend` | 弱依赖插件列表 |
| `loadBefore` | 先于哪些插件加载 |
| `provides` | 提供的功能标识 |
| `filePath` | 插件文件绝对路径 |
| `fileName` | 插件文件名 |

### PluginInfo

用于插件列表展示，公开字段：

| 字段 | 说明 |
|---|---|
| `name` / `version` / `author` / `description` | 基础信息 |
| `enabled` | 是否启用 |
| `listenerCount` | 监听方法数量 |
| `commandCount` | 注册命令数量 |
| `taskCount` | 登记任务数量 |

### PluginLogger

| 方法 | 说明 |
|---|---|
| `sendInfo(String)` | 普通日志 |
| `sendWarn(String)` | 警告日志 |
| `sendError(String)` | 错误日志 |
| `sendException(Throwable)` | 异常堆栈 |

### PluginState

```text
LOADED
ENABLED
DISABLED
FAILED
```

## 7. 插件管理器

| 方法 | 说明 |
|---|---|
| `getPlugin(String)` | 按名称获取插件 |
| `getPlugins()` | 获取已实例化插件 |
| `isEnabled(String)` | 查询插件是否启用 |
| `registerListener(Plugin,Listener)` | 注册监听器 |
| `registerCommand(Plugin,BotCommand)` | 注册聊天命令 |
| `registerService(Plugin,PluginService)` | 注册公共服务 |
| `getService(String)` | 按名称获取公共服务 |
| `runTask(Plugin,Runnable)` | 异步执行一次 |
| `runTaskLater(Plugin,Runnable,long)` | 延迟执行 |
| `runTaskTimer(Plugin,Runnable,long,long)` | 周期执行 |
| `enablePlugin(String)` | 启用插件 |
| `disablePlugin(String)` | 停用插件 |
| `reloadPlugin(String)` | 重载插件 |

所有注册方法都必须传当前插件实例。插件停用或重载时，主程序按插件回收资源。

## 8. 事件

监听器实现 `Listener`，监听方法必须满足：

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onEvent(SomeEvent event) {
}
```

规则：

- 方法必须只有一个事件参数。
- 事件参数必须继承 `Event`。
- 监听器异常会被捕获，不影响其他监听器。
- `MONITOR` 只用于观察，不修改事件或业务数据。
- 事件默认同步分发；耗时操作使用 `PluginManager` 的任务方法。

### 事件类型

| 事件类 | 对应 OneBot 事件 |
|---|---|
| `GroupMessageEvent` | 群消息 |
| `PrivateMessageEvent` | 私聊消息 |
| `NoticeEvent` | 通知 |
| `RequestEvent` | 请求 |
| `MetaEvent` | 元事件 |
| `RawEvent` | 未识别事件 |

### EventPriority

```text
LOWEST
LOW
NORMAL
HIGH
HIGHEST
MONITOR
```

### 消息事件字段

| 字段 | 说明 |
|---|---|
| `messageID` | 消息 ID |
| `userID` | 发送者 QQ |
| `rawMessage` | 原始文本消息 |
| `message` | OneBot 消息段数组 |
| `sender` | 发送者信息 |
| `raw` | 原始事件 JSON |

`GroupMessageEvent` 额外包含 `groupID`。

### 通知与请求

`NoticeEvent` 提供：

```text
noticeType
subType
userID
groupID
operatorID
targetID
raw
```

`RequestEvent` 提供：

```text
requestType
subType
userID
groupID
comment
flag
raw
```

## 9. 聊天命令

### CommandPermission

从高到低：

```text
OWNER
BOT_ADMIN
GROUP_OWNER
GROUP_ADMIN
EVERYONE
```

### BotCommand

```java
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

    public List<String> usage() {
        return Collections.emptyList();
    }

    public abstract boolean execute(CommandSender sender,String[] args);
}
```

`prefix()` 的第一个元素是命令名，其余元素是别名。

`execute` 的布尔返回值当前保留，命令是否命中由注册表决定。插件应返回 `true`。

### CommandSender

| 方法 | 说明 |
|---|---|
| `isGroup()` | 是否群聊 |
| `getGroupID()` | 群号，私聊为 0 |
| `getUserID()` | 用户 QQ |
| `getName()` | 用户昵称 |
| `getCommandPrefix()` | 命令前缀 |
| `hasPermission(CommandPermission)` | 二次权限判断 |
| `sendMessage(String)` | 发送文本 |
| `reply(String)` | 回复文本，目前等同 `sendMessage` |
| `sendImage(String)` | 发送图片 |

图片 `file` 支持 OneBot 可识别的：

```text
base64://
file://
http://
https://
```

### CommandInfo

供 `/help` 等插件展示：

```text
name
aliases
description
permission
source
usages
```

## 10. OneBot 客户端

`OneBotClient` 的方法同步等待 Action 响应。

| 方法 | 说明 |
|---|---|
| `sendGroupMessage(long,JSONArray)` | 发送群消息 |
| `sendPrivateMessage(long,JSONArray)` | 发送私聊消息 |
| `deleteMessage(long)` | 撤回消息 |
| `getGroupList()` | 群列表 |
| `getGroupMemberInfo(long,long)` | 群成员信息 |
| `setGroupBan(long,long,long)` | 群禁言 |
| `callAction(String,JSONObject)` | 调用任意 OneBot Action |
| `isConnected()` | 连接状态 |

失败时返回 `null` 或原始响应 JSON，不向插件抛出连接异常。

插件不要在事件监听方法里执行长时间阻塞调用。

### MessageUtil

| 方法 | 说明 |
|---|---|
| `message(JSONObject...)` | 组合消息段数组 |
| `text(String)` | 文本消息段 |
| `image(String)` | 图片消息段 |
| `at(long)` | @ 消息段 |
| `reply(long)` | 回复消息段 |
| `face(int)` | QQ 表情消息段 |
| `record(String)` | 语音消息段 |
| `segment(String,JSONObject)` | 自定义消息段 |

## 11. 插件配置

插件配置由 `Plugin.getConfig()` 获取。

支持一层键值 YAML：

```yaml
enableWelcome: true
welcomeText: "你好！"
```

| 方法 | 说明 |
|---|---|
| `getPath()` | 配置文件路径 |
| `load()` | 重新读取 |
| `save()` | 写回已修改的键 |
| `getString` / `getInt` / `getLong` / `getBoolean` | 类型读取 |
| `set(String,String)` | 修改键值 |
| `contains(String)` | 是否存在 |
| `keys()` | 键集合 |

写回保留注释、空行、键顺序与换行风格。

## 12. 数据存储

### 小型键值数据

| 方法 | 说明 |
|---|---|
| `get(Plugin,String)` | 读取 |
| `set(Plugin,String,String)` | 写入 |
| `remove(Plugin,String)` | 删除 |

### 结构化数据

| 方法 | 说明 |
|---|---|
| `query(String,Object...)` | 查询多行 |
| `queryOne(String,Object...)` | 查询一行 |
| `update(String,Object...)` | 更新或删除 |
| `insert(String,Object...)` | 插入 |

规则：

- 插件自定义表必须包含 `plugin_` 前缀。
- 插件 SQL 不允许访问 `bot_` 主程序表。
- 一律使用 `PreparedStatement` 占位符，不拼接用户输入。
- 插件不持有 JDBC 连接。

## 13. 图片与文本工具

### ImageUtil

| 方法 | 说明 |
|---|---|
| `renderText(String,List<String>)` | 渲染浅色主题 PNG |
| `toBase64Uri(byte[])` | 转换为 `base64://` 图片地址 |

### Text

| 方法 | 说明 |
|---|---|
| `safe(String)` | null 转空串 |
| `empty(String)` | 判断空白 |
| `join(List<String>,String)` | 连接字符串 |

## 14. 线程与异常约定

- 插件事件监听默认运行在调度线程。
- 插件长任务使用 `PluginManager` 的 `runTask` 系列方法。
- 插件任务异常只记录日志，不拖垮主程序。
- 插件注册资源在停用时统一回收。
- 插件自己创建的资源必须自己释放。

## 15. API 冻结检查

主程序 `build.ps1` 会运行 `ApiFreezeTest`。

该测试把全部 API 0.3 公开类、字段、构造器和方法生成签名哈希：

```text
API_FREEZE_HASH=5e10009c6a4ad44594cc14318ee4cb07cc247aacaf65da902ddb4f7201b4aa31
```

如果公共 API 形状变化但哈希没有更新，构建会失败。

只有以下情况可以更新哈希：

1. 非破坏性新增接口，并保持旧 API 兼容。
2. 破坏性变更同时进位 API 次版本，并同步全部文档与示例。

## 16. 官方示例

官方示例仓库：

```text
https://github.com/MossCG/MBB-ExamplePlugin
```

示例覆盖：

- 生命周期
- 配置读取与写回
- 聊天命令与权限
- 群聊、私聊、通知、请求、元事件和原始事件
- 文本、图片和组合消息发送
- 图片渲染
- 插件存储
- 延迟任务和周期任务
- 服务门面查询

开发新插件时，优先复制官方示例仓库结构。
