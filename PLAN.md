# MoBoxBot 计划书

> 状态：确认版 v0.4
> 日期：2026-09-30
> 范围：只定义方案、边界、模块、里程碑与验收标准，不包含实现代码。

## 1. 项目定位

MoBoxBot 是一个基于 Java 的 QQ 机器人客户端。

- QQ 协议端：NapCatQQ
- 通信协议：OneBot 11
- Java 侧：自建客户端
- 功能组织：Spigot 式插件加载
- 代码风格：参考 `MoBoxPanel` 与个人技能 `my-code-style`

核心原则：

1. 协议端与业务端解耦，NapCatQQ 可以替换，Java 业务逻辑不受影响。
2. 插件只依赖 `API` 包，不碰主程序内部实现。
3. 保持 MoBoxPanel 系代码风格：Java 8、`BasicInfo` 全局状态、静态工具类、中文注释与日志、零依赖注入。
4. 插件加载模仿 Spigot：`plugin.json`、`Plugin` 基类、生命周期、事件监听、命令注册、独立类加载器。
5. 先保证可观测、可恢复、可测试，再扩展功能。
6. 暂不做 Web 管理，放到最后作为可选项。

## 2. 已确认决策

| 项目 | 决策 |
|---|---|
| QQ 协议端 | NapCatQQ |
| 通信协议 | OneBot 11 |
| Java 侧 | 自建客户端 |
| 插件内核 | 模仿 Spigot / MoBoxPanel 自研，不用 PF4J |
| Java 版本 | Java 8 |
| 包名 | `org.moboxlab.moboxbot` |
| 构建方式 | `src/main/java` + `javac` + `build.ps1` + `out/` fat jar |
| 基础库 | 复用 `MossLib.jar`，保持与 MoBoxPanel 一致 |
| 新增依赖 | Java-WebSocket 1.6.0 |
| 数据存储 | SQLite |
| Web 管理 | 暂不做，最后作为可选项 |
| 默认连接模式 | 正向 WebSocket |
| 插件热重载 | 只在开发模式启用 |
| 运行目录 | `./MoBoxBot/` |
| 版本规则 | `V大版本.小版本.小更新.小修正.四位时间戳` |
| 插件 API 版本 | 从 `0.1` 开始 |

## 3. 目标与非目标

### 3.1 目标

- 稳定连接 NapCatQQ，支持反向 WebSocket 和正向 WebSocket。
- 实现 OneBot 11 的事件接收、消息解析和 API 调用。
- 支持 Spigot 式插件从 `plugins/` 目录加载、启用、停用和重载。
- 提供聊天命令注册、权限校验、冷却和参数解析。
- 提供全局配置和插件独立配置。
- 提供 SQLite 持久化、日志、状态查询和优雅关闭。
- 提供示例插件和插件开发文档。

### 3.2 首版非目标

- 不自己实现 QQ 协议。
- 不做 Web 管理。
- 不做插件市场。
- 不做多账号集群。
- 不提供插件沙箱。插件是可执行代码，默认只加载可信 JAR。
- 不引入 Spring、Lombok、PF4J、MyBatis 等重型框架。

## 4. 代码风格约束

这一节是 MoBoxBot 的硬约束，写代码前必须先读同目录的 `STYLE.md`。当前先以本计划为准，M0 阶段再整理成正式 `STYLE.md`。

### 4.1 三条底线

1. 保持一致比“更现代”更重要。
2. 中文注释、中文日志、中文配置说明；不用 emoji。
3. 后端只使用 JDK 8 + MossLib + fastjson + 必要的 WebSocket 库；不引入第二套 JSON、日志、配置框架。

### 4.2 包结构

根包 `org.moboxlab.moboxbot`，一个业务域一个子包，子包首字母大写：

```text
org.moboxlab.moboxbot
├─ Main.java                 进程入口 + 启动装配
├─ BasicInfo.java            全局状态集中地
├─ API/                      插件唯一依赖的公开接口
├─ Command/                  控制台命令
├─ Database/                 SQLite 入口与 SQL 封装
├─ OneBot/                   OneBot 11 连接、事件、消息与 Action
├─ Plugin/                   插件内核、类加载器、事件总线、注册表
├─ Task/                     定时任务框架
└─ Util/                     文本、JSON、文件等工具
```

命名规律：

| 类型 | 命名 | 示例 |
|---|---|---|
| 模块入口 | `XxxMain` | `Main`、`OneBotMain`、`DatabaseMain` |
| 插件接口 | `PluginXxx` | `PluginManager`、`PluginDescription`、`PluginState` |
| 插件实现 | `PluginXxxImpl` | `PluginManagerImpl` |
| 事件类 | `XxxEvent` | `GroupMessageEvent`、`BotOnlineEvent` |
| 控制台命令 | `CommandXxx` | `CommandStatus`、`CommandPlugin` |
| 工具类 | `XxxUtil` / `Xxx` | `MessageUtil`、`TextUtil` |

### 4.3 Java 写法

- Java 8 语法且克制：不用 `var`、不用 `.stream()`、不用 Lombok、不用 `Optional`。
- 业务工具类全部静态：没有构造器、不写接口与抽象类。
- 只有插件 API、事件模型、框架必须扩展的地方允许接口与抽象类。
- 跨模块调用一律 `XxxClass.method(...)` 静态调用，不做依赖注入。
- 全局状态集中在 `BasicInfo`：`logger`、`config`、`pluginManager`、`oneBotClient`、`database`、`debug`、`version`、`startTime`。
- 4 空格缩进，K&R 大括号。
- 逗号后不加空格：`update(data,responseData);`。
- Java 字符串拼接 `+` 两侧不加空格：`"启动完成！耗时："+(completeTime-startTime)+"毫秒！"`。
- `List` / `Map` / `String[]` 显式声明类型。
- 简单校验直接一行：`if (data == null) return;`。
- 方法内用分节注释分段，例如 `//配置读取`、`//数据库操作`、`//响应填入`。

### 4.4 异常与日志

统一日志出口：

```java
BasicInfo.logger.sendInfo("正在启动 OneBot 模块......");
BasicInfo.logger.sendWarn("插件配置缺少 token，已使用空值！");
BasicInfo.logger.sendException(e);
BasicInfo.sendDebug("收到群消息："+groupID);
```

规则：

- 零 `System.out`，零 `printStackTrace`。
- `sendInfo` 记录正常流程，`sendWarn` 记录可恢复问题，`sendException` 记录异常，`sendDebug` 受 debug 开关控制。
- 日志文案口语化、带感叹号，允许“哦~”“喵”这类语气，但管理端相关文案保持简洁。
- 插件异常只能记日志，不能拖垮主程序。

### 4.5 配置与 SQL

- 配置读取统一 `BasicInfo.config.getString/getBoolean/getInteger("camelCaseKey")`，键名与 `config.yml` 完全一致。
- 写回用户配置文件必须逐行替换，保留注释、空行、键顺序与换行风格，只改已存在的键。
- SQL 一律 `PreparedStatement` + `?` 占位符，列名加反引号，禁止新增字符串拼接的 SQL。
- SQLite 操作统一走 `SqlExecutor`，业务代码不直接持有连接。
- 动用户数据的操作要记日志。

### 4.6 插件代码边界

- 插件只允许依赖 `org.moboxlab.moboxbot.API`。
- 插件禁止直接调用 `BasicInfo`、`Database`、`OneBot` 内部类。
- 插件要读自己的 JAR 资源，统一用 `readResource` / `readResourceText`，不要用会命中父加载器的 `getResourceAsStream`。
- 插件 JAR 不要打包 MossLib、fastjson、sqlite-jdbc、Java-WebSocket。
- 插件注册的一切资源由主程序按插件记账，停用和重载时统一回收。

### 4.7 版本与构建

- 版本号格式：`V大版本.小版本.小更新.小修正.四位时间戳`，例如 `V0.0.1.0.1930`。
- 开发阶段大版本固定为 `0`，正式发布才进位到 `1`。
- 只用 `build.ps1` 构建，产物放 `out/`，不依赖 IDE 编译。
- 编译命令必须带 `-encoding UTF-8`。
- 每轮功能完成后更新版本号、`update.md` 和 `BasicInfo.version`。

## 5. 总体架构

```text
QQ 客户端
    |
    v
NapCatQQ
    |
    | OneBot 11: WebSocket / HTTP
    v
OneBot 模块
    |
    +--> 事件解析 --> EventBus --> 插件事件监听器
    |
    +--> Action API --> 发送队列 --> OneBot 响应
    |
PluginManager
    |
    +--> Plugin A
    +--> Plugin B
    +--> Built-in Plugin
    |
基础服务
    |
    +--> Config
    +--> SQLite
    +--> Scheduler
    +--> Logger
```

分层职责：

| 层 | 职责 |
|---|---|
| OneBot | WebSocket 连接、认证、心跳、重连、echo 关联、Action 调用 |
| Plugin | 扫描、依赖排序、类加载、生命周期、注册表、资源回收 |
| API | 插件唯一依赖的公开接口与事件模型 |
| Command | 控制台命令与聊天命令 |
| Database | SQLite 初始化、建表、SQL 执行 |
| Task | 定时任务与异步任务 |

## 6. 目录与模块

### 6.1 仓库结构

```text
MoBoxBot/
├─ AGENTS.md
├─ STYLE.md
├─ PLUGIN.md
├─ PLAN.md
├─ build.ps1
├─ run.bat
├─ pom.xml                    # 可选，只给 IDE 识别
├─ _ref/                      # 本地参考资料，不提交
│  ├─ NapCatQQ/
│  ├─ NapCatDocs/
│  ├─ napneko.github.io/
│  ├─ Java-WebSocket-1.6.0/
│  └─ Java-WebSocket-1.5.7/
├─ depend/
│  ├─ MossLib.jar
│  └─ Java-WebSocket-1.6.0.jar
├─ src/
│  ├─ main/
│  │  ├─ java/org/moboxlab/moboxbot/
│  │  └─ resources/
│  │     ├─ config.yml
│  │     └─ struct-sqlite.sql
│  └─ test/
│     └─ java/                # 轻量测试类与 Mock OneBot
└─ out/
```

### 6.2 运行目录

```text
./MoBoxBot/
├─ config.yml
├─ data/
│  └─ bot.db
├─ logs/
├─ plugins/
│  ├─ MoBoxBot-ExamplePlugin.jar
│  └─ MoBoxBot-ExamplePlugin/
│     └─ config.yml
└─ dependency/
```

启动时用 `FileCheck.checkDirExist` / `FileCheck.checkFileExist` 从 JAR 释放模板文件。配置、页面、建表脚本只在文件不存在时释放，升级时按 MoBoxPanel 的习惯提醒部署方处理。

### 6.3 主程序模块

| 模块 | 内容 |
|---|---|
| `Main` | 启动装配、控制台命令注册、热重载入口 |
| `BasicInfo` | 全局状态与安全配置读取 |
| `OneBot` | 连接、事件、消息、Action、NapCat 扩展适配 |
| `Plugin` | Spigot 式插件内核 |
| `API` | 插件公开接口、事件、命令、配置、权限 |
| `Command` | 控制台命令实现 |
| `Database` | `DatabaseMain`、`SqlExecutor`、`TableInitializer` |
| `Task` | `SchedulerService` |
| `Util` | 文本、JSON、时间、文件工具 |

## 7. OneBot 11 接入设计

### 7.1 连接模式

首版支持两种模式，统一由 `OneBotMain` 管理：

| 模式 | 方向 | 适用场景 | 建议 |
|---|---|---|---|
| 正向 WebSocket | Java 客户端连接 NapCat | 本地部署、Java 端更容易访问 NapCat | 默认推荐 |
| 反向 WebSocket | NapCat 连接 Java 客户端 | Java 端有固定端口，NapCat 在另一台机器 | 备选 |
| HTTP API | Java 端主动调用 HTTP | 只把 WS 用于事件，API 走 HTTP | 后续扩展 |

正向 WebSocket 配置：

```yaml
oneBotMode: "forward-ws"
oneBotUrl: "ws://127.0.0.1:3001"
oneBotToken: ""
```

反向 WebSocket 配置：

```yaml
oneBotMode: "reverse-ws"
oneBotHost: "0.0.0.0"
oneBotPort: 3001
oneBotPath: "/onebot"
oneBotToken: ""
```

NapCat 侧的正向 WS 配置来自 `websocketServers`，默认结构如下：

```json5
{
  "network": {
    "websocketServers": [
      {
        "name": "MoBoxBot",
        "enable": true,
        "host": "127.0.0.1",
        "port": 3001,
        "messagePostFormat": "array",
        "reportSelfMessage": false,
        "token": "",
        "enableForcePushEvent": true,
        "debug": false,
        "heartInterval": 30000
      }
    ]
  }
}
```

MoBoxBot 默认连接 `ws://127.0.0.1:3001`。生产环境必须设置非空 `token`，并避免把 WS 端口直接暴露到公网。具体字段以实际安装版本为准，Java 侧只依赖 OneBot 11 协议，不依赖 NapCat 内部实现。

### 7.2 认证

- WebSocket 连接支持 `Authorization: Bearer <token>`。
- Token 从配置读取，不允许硬编码。
- 日志中不输出 Token。
- 认证失败记录明确原因并拒绝连接。

### 7.3 心跳与重连

- 记录最近一次 `meta_event.heartbeat`。
- 超过阈值未收到心跳，判定连接异常。
- 正向模式使用指数退避重连，初始 1 秒，最大 60 秒。
- 反向模式由 NapCat 重连，Java 端负责释放旧连接和清理会话。
- 重连成功后触发 `BotOnlineEvent`，不重复投递未完成事件。

### 7.4 Action API

统一静态入口 `OneBotClient`：

```java
OneBotClient.sendGroupMessage(groupID,message);
OneBotClient.sendPrivateMessage(userID,message);
OneBotClient.deleteMessage(messageID);
OneBotClient.getGroupList();
OneBotClient.getGroupMemberInfo(groupID,userID);
OneBotClient.setGroupBan(groupID,userID,duration);
```

设计要求：

1. 每个请求生成唯一 `echo`。
2. 用 `LinkedHashMap` + 锁记录等待中的响应，超时后清理。
3. 同步方法返回 `JSONObject`，异步方法用 `SchedulerService.runTaskAsync`。
4. 发送类接口统一走队列和频率限制。
5. 只对幂等接口做自动重试，非幂等接口不盲目重试。
6. `retcode` 非成功时保留原始响应，方便插件判断。

### 7.5 事件范围

首版覆盖：

- `message.private`
- `message.group`
- `notice.group_upload`
- `notice.group_admin`
- `notice.group_decrease`
- `notice.group_increase`
- `notice.group_ban`
- `notice.friend_add`
- `notice.group_recall`
- `notice.friend_recall`
- `notice.poke`
- `request.friend`
- `request.group`
- `meta_event.lifecycle`
- `meta_event.heartbeat`

未识别事件不能丢弃，应进入原始事件通道，供插件按需处理。

### 7.6 消息模型

为了保持 MoBoxPanel 的 JSON 风格，消息统一使用 fastjson：

- 事件原始数据：`JSONObject`
- 消息段：`JSONArray`
- 文本：`MessageUtil.text("hello")`
- 图片：`MessageUtil.image("file:///...")`
- @：`MessageUtil.at(userID)`
- 回复：`MessageUtil.reply(messageID)`
- 组合：`MessageUtil.message(segment1,segment2)`

插件不直接拼复杂 JSON 字符串，统一通过 `MessageUtil` 构造消息。

### 7.7 NapCat 扩展

1. 核心只实现标准 OneBot 11。
2. NapCat 扩展 API 放入 `OneBot` 的扩展适配层。
3. 扩展能力通过 `OneBotClient.callAction` 暴露，插件不直接依赖 NapCat SDK。
4. 启动时可通过 `get_version_info` 做能力探测。

### 7.8 NapCat 文档参考

文档已拉到本地参考目录，M0 和实现阶段以这些内容为准：

| 内容 | 路径 |
|---|---|
| NapCat 源码 | `_ref/NapCatQQ` |
| NapCat 文档源 | `_ref/NapCatDocs` |
| 构建后的文档站 | `_ref/napneko.github.io` |
| OneBot 网络基础 | `_ref/NapCatDocs/src/onebot/network.md` |
| OneBot 事件结构 | `_ref/NapCatDocs/src/onebot/event.md` |
| OneBot 消息段 | `_ref/NapCatDocs/src/onebot/segment.md` |
| NapCat API 列表 | `_ref/NapCatDocs/src/onebot/api.md` |
| NapCat 差异说明 | `_ref/NapCatDocs/src/onebot/napcat.md` |
| NapCat 网络配置 | `_ref/NapCatDocs/src/config/basic.md` |
| NapCat 源码配置结构 | `_ref/NapCatQQ/packages/napcat-onebot/config/config.ts` |

关键结论：

- 正向 WS 是 NapCat 作为 WebSocket 服务端，Java 作为客户端主动连接。
- 反向 WS 是 NapCat 作为 WebSocket 客户端，主动连接 Java 的 WebSocket 服务端。
- `messagePostFormat` 固定用 `array`，保证消息段结构稳定。
- `token` 必须支持，且生产环境不得留空。
- 2025-09-05 的 OneBot 安全事件说明，空 Token、公网暴露的实例会被批量扫描和滥用，部署文档必须写明这一点。
- API 文档有版本快照，`_ref/NapCatDocs/src/api/` 下当前最高版本是 `4.18.28`，实现时以实际 NapCat 版本与最新快照交叉确认。

## 8. 插件系统设计

### 8.1 设计目标

模仿 Spigot 和 MoBoxPanel：

- 插件是一个 JAR 包。
- JAR 根目录带 `plugin.json`。
- 主类继承 `org.moboxlab.moboxbot.API.Plugin`。
- 主程序扫描 `plugins/*.jar`，读元数据、排序依赖、建类加载器、实例化、加载、启用。
- 插件注册的事件、命令、任务全部按插件记账，停用和重载时统一回收。
- 单个插件失败绝不影响主程序启动。

### 8.2 plugin.json

```json
{
  "name": "MoBoxBot-ExamplePlugin",
  "version": "V0.0.1.0.1930",
  "apiVersion": "0.1",
  "main": "org.moboxlab.example.ExamplePlugin",
  "author": "MossCG",
  "description": "MoBoxBot 示例插件",
  "website": "",
  "depend": [],
  "softDepend": [],
  "loadBefore": [],
  "provides": ["example"]
}
```

| 字段 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 唯一标识，同时是插件数据目录名与依赖声明名 |
| `version` |  | 插件版本，沿用主程序版本规则 |
| `apiVersion` | 是 | 依赖的插件 API 版本，主版本必须一致 |
| `main` | 是 | 主类全限定名，必须继承 `API.Plugin` |
| `author` / `description` / `website` |  | 展示用 |
| `depend` |  | 强依赖：缺失或加载失败则本插件不加载 |
| `softDepend` |  | 弱依赖：存在则先加载，不存在也继续 |
| `loadBefore` |  | 反向声明：希望在某个插件之前加载 |
| `provides` |  | 本插件提供的功能标识 |

### 8.3 生命周期

```java
public class ExamplePlugin extends Plugin {
    @Override
    public void onLoad() {
        //可以读配置、注册监听器；不要访问别的插件
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        //依赖已就绪：注册监听器、命令、任务
        getServer().getPluginManager().registerListener(this,new ExampleListener(this));
        getServer().getPluginManager().registerCommand(this,new ExampleCommand());
    }

    @Override
    public void onDisable() {
        //只做自己的收尾；登记过的资源由主程序统一回收
    }
}
```

硬规矩：插件不要把异常抛给主程序。失败时自己 `getLogger().sendWarn(...)` 并返回 `null` / `false`，主程序会把状态置为 `FAILED` 并记录原因。

### 8.4 插件管理器

`API.PluginManager` 提供：

| 方法 | 用途 |
|---|---|
| `registerListener(plugin,listener)` | 注册事件监听器 |
| `registerCommand(plugin,command)` | 注册聊天命令 |
| `runTask(plugin,task)` | 异步执行一次 |
| `runTaskLater(plugin,task,delaySeconds)` | 延迟执行 |
| `runTaskTimer(plugin,task,delay,period)` | 周期执行 |
| `getPlugin(name)` | 按名字取插件 |
| `getPlugins()` | 取全部插件 |
| `isEnabled(name)` | 查询插件状态 |
| `enablePlugin(name)` / `disablePlugin(name)` / `reloadPlugin(name)` | 插件管理 |
| `getOneBotClient()` | 取 OneBot 客户端门面 |

### 8.5 服务门面

`API.Server` 是插件访问主程序的唯一入口：

```java
getVersion();
getApiVersion();
getBotName();
getPluginManager();
getEventBus();
getOneBotClient();
getLogger();
```

插件不要直接访问 `BasicInfo` 或内部实现类。

### 8.6 事件总线

事件总线模仿 MoBoxPanel：

- 监听器实现 `API.Event.Listener`。
- 方法上加 `@EventHandler(priority=...,ignoreCancelled=...)`。
- 分发默认同步，保证顺序语义。
- 耗时操作用 `callEventAsync` 或 `runTask`。
- 监听器抛异常只记日志，不影响其他监听器。
- 优先级：`LOWEST`、`LOW`、`NORMAL`、`HIGH`、`HIGHEST`、`MONITOR`。
- `MONITOR` 只用于观察记录，不要改业务数据。

事件示例：

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onGroupMessage(GroupMessageEvent event) {
    //业务逻辑
}
```

### 8.7 聊天命令

聊天命令 API 参考 Spigot 的命令模型：

```java
public class PingCommand extends BotCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("ping");
        prefixList.add("p");
        return prefixList;
    }

    @Override
    public boolean execute(CommandSender sender,String[] args) {
        sender.sendMessage("pong");
        return true;
    }
}
```

命令路由流程：

```text
收到消息
  → 取命令前缀
  → 匹配命令名与别名
  → 校验权限
  → 校验冷却
  → 参数解析
  → execute
```

权限等级：

| 等级 | 含义 |
|---|---|
| `OWNER` | 机器人所有者 |
| `BOT_ADMIN` | 机器人管理员 |
| `GROUP_OWNER` | 群主 |
| `GROUP_ADMIN` | 群管理员 |
| `EVERYONE` | 所有人 |

### 8.8 类加载规则

每个插件一个 `PluginClassLoader`，继承 `URLClassLoader`，策略与 MoBoxPanel 一致：

| 包 | 由谁加载 | 原因 |
|---|---|---|
| `java.*` / `javax.*` / `com.sun.*` | 父加载器 | JRE |
| `com.alibaba.fastjson.*` | 父加载器 | 保证插件与主程序是同一个 `JSONObject` 类 |
| `org.moboxlab.moboxbot.API.*` | 父加载器 | 接口只有一个定义 |
| 主程序其他包 | 父加载器 | 宽松放开，约定插件不要用 |
| 插件自己的包 | 插件加载器 | 插件 JAR |

派生规则：

1. 插件 JAR 不要打包 MossLib、fastjson、sqlite-jdbc、Java-WebSocket。
2. 插件读自己的资源用 `readResource` / `readResourceText`。
3. 插件线程、连接、定时任务必须在 `onDisable` 中释放。
4. 生产环境更新插件建议重启进程，热重载只保证主程序不重启。

### 8.9 资源记账与回收

`PluginRecord` 记录插件注册过的一切资源：

- `listeners`
- `commands`
- `tasks`
- `apiExtensions`
- `dataFolder`
- `classLoader`
- `state`
- `message`

停用或重载时按记录逐项回收，避免“旧监听器还在跑”“旧命令还能用”这类幽灵问题。

## 9. 命令与权限

### 9.1 控制台命令

控制台命令继续使用 MossLib 的 `ObjectCommand` 与 `CommandManager`，与 MoBoxPanel 一致。

首版命令：

- `status`
- `plugin list`
- `plugin info <name>`
- `plugin enable <name>`
- `plugin disable <name>`
- `plugin reload <name>`
- `debug`
- `exit`

### 9.2 聊天命令

聊天命令使用 `API.Command.BotCommand`，支持：

- 命令前缀，例如 `/`、`!`
- 命令别名
- 子命令
- 参数类型：字符串、整数、QQ 号、@、群号、图片、回复消息、剩余文本
- 用户冷却与群冷却
- 全局黑白名单
- 群级黑白名单
- 命令帮助自动生成

### 9.3 权限配置

权限数据存 SQLite，启动时加载到内存缓存：

- `bot_owner`：机器人所有者
- `bot_admin`：机器人管理员
- `group_config`：群配置、命令开关、黑名单
- `user_permission`：用户自定义权限

插件通过 `CommandSender.hasPermission(...)` 判断，不直接读数据库。

## 10. 配置与数据

### 10.1 全局配置

`config.yml` 放在仓库 `src/main/resources/`，首次运行释放到 `./MoBoxBot/config.yml`。

```yaml
#基础设置
botName: "MoBoxBot"
botOwner: "123456789"
commandPrefix: "/"
debug: false

#OneBot 连接
oneBotMode: "reverse-ws"
oneBotHost: "0.0.0.0"
oneBotPort: 3001
oneBotPath: "/onebot"
oneBotToken: ""

#数据库
databaseType: "sqlite"
sqlitePath: "./MoBoxBot/data/bot.db"
sqlitePoolSize: 2

#插件
pluginDir: "./MoBoxBot/plugins"
pluginAutoEnable: true
pluginHotReload: false
```

读取统一用 `BasicInfo.getConfigString/getConfigInt/getConfigBoolean`。

### 10.2 插件配置

插件配置放在：

```text
./MoBoxBot/plugins/<插件名>/config.yml
```

插件配置 API 沿用 MoBoxPanel 的 `PluginConfig` 风格：

- 只认 `键: 值` 一层结构。
- 写回时逐行替换。
- 保留注释、空行、键顺序和换行风格。
- 布尔与整数裸写，其余加双引号。

### 10.3 SQLite

数据库入口模仿 MoBoxPanel：

```text
DatabaseMain.init()
  → SQLiteManager.initSQLite(...)
  → BasicInfo.logger.sendInfo(...)

SqlExecutor.query/queryOne/update/insert/execute/transaction
```

规则：

- 所有 SQL 统一走 `SqlExecutor`。
- 串行执行，避免 SQLite 写锁冲突。
- 一律 `PreparedStatement` + `?`。
- 列名加反引号。
- 建表脚本 `struct-sqlite.sql` 只在文件不存在时释放。

首版表结构如下，结构版本用 `PRAGMA user_version` 管理，初始为 `1`：

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS `bot_plugin_record` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT NOT NULL,
    `version` TEXT NOT NULL DEFAULT '',
    `apiVersion` TEXT NOT NULL DEFAULT '',
    `state` TEXT NOT NULL DEFAULT 'LOADED',
    `loadTime` INTEGER NOT NULL DEFAULT 0,
    `message` TEXT NOT NULL DEFAULT '',
    `fileName` TEXT NOT NULL DEFAULT '',
    `filePath` TEXT NOT NULL DEFAULT '',
    `dataFolder` TEXT NOT NULL DEFAULT '',
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_plugin_record_name` ON `bot_plugin_record` (`name`);

CREATE TABLE IF NOT EXISTS `bot_group_config` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `groupID` TEXT NOT NULL,
    `enable` INTEGER NOT NULL DEFAULT 1,
    `commandPrefix` TEXT NOT NULL DEFAULT '',
    `welcomeEnable` INTEGER NOT NULL DEFAULT 0,
    `leaveEnable` INTEGER NOT NULL DEFAULT 0,
    `antiRecallEnable` INTEGER NOT NULL DEFAULT 0,
    `extra` TEXT NOT NULL DEFAULT '{}',
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_group_config_group` ON `bot_group_config` (`groupID`);

CREATE TABLE IF NOT EXISTS `bot_group_user` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `groupID` TEXT NOT NULL,
    `userID` TEXT NOT NULL,
    `permission` TEXT NOT NULL DEFAULT 'EVERYONE',
    `enable` INTEGER NOT NULL DEFAULT 1,
    `lastActive` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_group_user_unique` ON `bot_group_user` (`groupID`,`userID`);

CREATE TABLE IF NOT EXISTS `bot_user` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `userID` TEXT NOT NULL,
    `nickname` TEXT NOT NULL DEFAULT '',
    `permission` TEXT NOT NULL DEFAULT 'EVERYONE',
    `enable` INTEGER NOT NULL DEFAULT 1,
    `lastActive` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_user_user` ON `bot_user` (`userID`);

CREATE TABLE IF NOT EXISTS `bot_plugin_data` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `pluginName` TEXT NOT NULL,
    `dataKey` TEXT NOT NULL,
    `dataValue` TEXT NOT NULL DEFAULT '',
    `expireTime` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_plugin_data_unique` ON `bot_plugin_data` (`pluginName`,`dataKey`);
CREATE INDEX IF NOT EXISTS `idx_bot_plugin_data_expire` ON `bot_plugin_data` (`expireTime`);

CREATE TABLE IF NOT EXISTS `bot_command_log` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `messageID` TEXT NOT NULL DEFAULT '',
    `groupID` TEXT NOT NULL DEFAULT '',
    `userID` TEXT NOT NULL DEFAULT '',
    `command` TEXT NOT NULL DEFAULT '',
    `args` TEXT NOT NULL DEFAULT '',
    `success` INTEGER NOT NULL DEFAULT 0,
    `costTime` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_bot_command_log_time` ON `bot_command_log` (`createTime`);
CREATE INDEX IF NOT EXISTS `idx_bot_command_log_group_time` ON `bot_command_log` (`groupID`,`createTime`);

CREATE TABLE IF NOT EXISTS `bot_message_log` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `messageID` TEXT NOT NULL DEFAULT '',
    `messageType` TEXT NOT NULL DEFAULT '',
    `groupID` TEXT NOT NULL DEFAULT '',
    `userID` TEXT NOT NULL DEFAULT '',
    `rawMessage` TEXT NOT NULL DEFAULT '',
    `createTime` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_bot_message_log_time` ON `bot_message_log` (`createTime`);

PRAGMA user_version = 1;
```

表职责：

| 表 | 用途 |
|---|---|
| `bot_plugin_record` | 插件加载状态、版本、失败原因 |
| `bot_group_config` | 群级开关、命令前缀、通用扩展配置 |
| `bot_group_user` | 群成员权限与活跃信息 |
| `bot_user` | 全局用户权限与昵称 |
| `bot_plugin_data` | 插件通用键值存储，带过期时间 |
| `bot_command_log` | 命令调用审计 |
| `bot_message_log` | 可选消息日志，默认关闭并定期清理 |

约定：

- QQ 号、群号、消息号统一存 `TEXT`，避免平台差异和整数溢出。
- 时间统一存毫秒时间戳 `INTEGER`。
- 布尔统一存 `0` / `1`。
- 插件自定义表统一使用 `plugin_<插件名>_` 前缀。
- 插件名先转小写，非 `a-z0-9` 字符替换为下划线，避免表名冲突。
- `bot_plugin_data` 用于小数据；结构化数据仍由插件建自己的表。

### 10.4 MossLib 复用结论

MoBoxPanel 的 `MossLib-APIDOC.md` 已确认 MossLib 是 fat jar，内置 SQLite、MySQL、fastjson、snakeyaml、log4j、oshi 和 slf4j-api。

直接复用：

| 模块 | 用法 |
|---|---|
| `ObjectLogger` | 统一日志出口，`sendInfo` / `sendWarn` / `sendException` / `sendAPI` |
| `ConfigManager` / `ObjectConfig` | 读取 `config.yml` |
| `SQLiteManager` | SQLite 连接池 |
| `CommandManager` / `ObjectCommand` | 控制台命令 |
| `FileCheck` | 目录与模板文件释放 |
| `EncryptSHA256` | 需要摘要时使用 |

不要使用：

| 模块 | 原因 |
|---|---|
| `SQLiteExecute.update/insert` | 参数实际不生效，带 `?` 会失败 |
| `SQLiteExecute.getResultSet` | 可用，但为了统一仍建议走 `SqlExecutor` |
| `EncryptMD5` | 不是标准 MD5，不要用于密码或签名 |
| `FileDependency.loadDependencyDir` 无条件调用 | jar 内没有 `dependency/` 时会打异常堆栈，先判断资源是否存在 |

必须规避：

| 问题 | 规避 |
|---|---|
| `SQLiteManager.getConnection()` 成功路径也 sleep 200ms | `DatabaseMain` 用 `ThreadLocal<Connection>` 缓存连接，与 MoBoxPanel 一致 |
| `FileCheck.checkDirExist` 只建一级目录 | `./MoBoxBot/data` 这类路径逐级创建 |
| `ObjectConfig` 缺键可能 NPE | `BasicInfo` 提供 `getConfigString/getConfigInt/getConfigBoolean` 安全读取 |
| 配置加载失败走 `printStackTrace` | 调用前先 `FileCheck` 校验，日志里补上下文 |

Java-WebSocket 固定 `1.6.0`：

- `1.6.0` 的 `sourceCompatibility` 是 Java 8。
- 它只使用 slf4j 的基础日志方法，可以被 MossLib 内置的 slf4j-api 1.7.36 满足。
- 打包时只引入 Java-WebSocket 本体，不引入它声明的 slf4j-api 2.x，避免和 MossLib 冲突。
- 如果 M0 空跑出现 slf4j binding 警告，再评估是否加入 `slf4j-nop 1.7.36`，不预先引入。
- `1.5.7` 仅作为对比参考留在 `_ref`，不作为最终依赖。

## 11. 日志、状态与运维

### 11.1 日志

- 使用 MossLib `ObjectLogger`，与 MoBoxPanel 一致。
- 控制台输出启动流程、连接状态、插件状态、错误原因。
- 文件日志放 `./MoBoxBot/logs/`。
- 日志包含：时间、级别、插件名、群号、用户号、事件类型。
- Action 调用记录耗时和 `retcode`。

### 11.2 状态

`status` 命令输出：

- 主程序版本与运行时长。
- OneBot 连接状态。
- 最近心跳时间。
- 事件队列长度。
- 发送队列长度。
- 插件数量与失败数量。
- JVM 内存。

`plugin list` 输出：

- 插件名、版本、API 版本。
- 状态：`LOADED` / `ENABLED` / `DISABLED` / `FAILED`。
- 注册的监听器、命令、任务数量。
- 失败原因。

### 11.3 优雅关闭

关闭顺序：

1. 停止接收新事件。
2. 等待事件队列处理完成，设置超时。
3. 依次停用插件，释放插件资源。
4. 关闭 OneBot WebSocket。
5. 关闭 SQLite。
6. 写入最终日志。

### 11.4 部署

支持：

- Windows 控制台启动，`run.bat`。
- Windows 服务启动，可选 WinSW 或 NSSM。
- Linux systemd。

部署文档覆盖：

- 安装 Java 8。
- 安装并登录 NapCatQQ。
- 配置 NapCat 的反向或正向 WebSocket。
- 配置 Token。
- 启动 MoBoxBot。
- 查看日志与执行 `status`。

## 12. 测试计划

### 12.1 测试原则

首版不自带 JUnit，避免新增测试依赖。核心逻辑用 `src/test/java` 下的独立 `TestMain` 类验证，需要模拟协议时启动 Mock OneBot WebSocket Server。

如果后续测试规模变大，再评估是否引入 JUnit，不在 M0 决定。

### 12.2 单元测试

- OneBot JSON 序列化与反序列化。
- 消息段构造与解析。
- 事件类型映射。
- 命令解析和参数绑定。
- 权限和冷却。
- 配置加载与逐行写回。
- SQL 参数绑定与事务。

### 12.3 集成测试

- Mock OneBot WebSocket Server。
- 模拟事件推送。
- 断言 Action 请求与 `echo` 关联。
- 模拟断线、重连、心跳超时。
- 模拟错误 `retcode`。

### 12.4 插件测试

- 构建示例插件 JAR。
- 从目录加载插件。
- 依赖排序与缺失依赖处理。
- 注册事件监听器与命令。
- 派发事件并验证结果。
- 停用、重载和资源回收。
- 单个插件失败不影响主程序启动。

### 12.5 端到端测试

- 真实 NapCatQQ 环境。
- 扫码登录后连接。
- 群消息、私聊消息、@ 消息。
- 图片、回复、撤回、戳一戳。
- 断线重连。
- 权限拒绝和冷却提示。

### 12.6 质量门槛

- `build.ps1` 编译通过。
- 核心测试类通过。
- 示例插件能加载、启用、注册命令和监听器。
- 发布前完成一次 Windows 或 Linux 端到端验证。

## 13. 里程碑

| 阶段 | 内容 | 交付物 | 验收 |
|---|---|---|---|
| M0 | 方案定稿 | 本计划书、`STYLE.md`、`PLUGIN.md`、API 接口草案 | 关键决策确认 |
| M1 | 工程骨架 | 目录、`build.ps1`、`run.bat`、配置、日志、`Main` | 能启动、能输出版本、能退出 |
| M2 | 数据库 | `DatabaseMain`、`SqlExecutor`、建表脚本 | SQLite 可读写 |
| M3 | OneBot 连接 | 反向 WS、正向 WS、认证、心跳、重连 | 能连接 NapCat |
| M4 | 事件与消息 | 事件模型、消息工具、Action 调用 | 能收群消息并回复 |
| M5 | 插件内核 | 扫描、`plugin.json`、依赖排序、类加载器、生命周期 | 能加载示例插件 |
| M6 | 事件与命令 | `EventBus`、`BotCommand`、权限、冷却 | 插件能监听事件并注册命令 |
| M7 | 示例与内置插件 | 示例插件、插件管理命令 | `plugin list` 可用 |
| M8 | 测试与部署 | Mock OneBot、测试类、部署文档 | 目标环境稳定运行 |
| M9 | 可选增强 | Web 管理、HTTP API、多账号 | 按需求评估 |

## 14. 风险与对策

| 风险 | 影响 | 对策 |
|---|---|---|
| NapCatQQ 版本变化 | 事件或 API 不兼容 | 协议适配层、能力探测、保留原始事件 |
| QQ 风控 | 账号限制或封禁 | 频率限制、随机延迟、避免批量操作、使用小号 |
| 插件类加载泄漏 | 内存持续增长 | 默认不热重载、资源释放规范、重载检查 |
| 插件依赖冲突 | 启动失败或行为异常 | API 父加载、插件依赖隔离、加载期校验 |
| MossLib 兼容性 | 与 MoBoxPanel 行为不一致 | M0 先验证日志、配置、命令、SQLite 四项能力 |
| Java-WebSocket 依赖 | 打包或运行缺类 | 固定版本放进 `depend/`，构建后做一次空跑 |
| SQLite 写锁 | 并发写失败 | 串行 `SqlExecutor`，事务短小 |
| 非幂等重试 | 重复发消息 | 只重试幂等接口，发送接口带请求标识 |
| 插件安全 | 恶意插件执行任意代码 | 只加载可信插件，首版不提供沙箱 |
| 许可证不兼容 | 发布和商用风险 | M0 阶段确认 MossLib、Java-WebSocket 许可证 |

## 15. 验收标准

首版达到以下标准才算可用：

1. 包名为 `org.moboxlab.moboxbot`，目录与构建方式符合 MoBoxPanel 风格。
2. 主程序用 Java 8 编译，`build.ps1` 能产出独立 JAR。
3. NapCatQQ 能以反向 WebSocket 或正向 WebSocket 连接 MoBoxBot。
4. Token 认证失败时拒绝连接并给出明确日志。
5. 能接收群消息、私聊消息和常见通知事件。
6. 能发送文本、图片、@、回复消息。
7. 断线后能自动重连，NapCat 重启后可恢复。
8. 能从 `plugins/` 加载 `plugin.json`，主类继承 `API.Plugin`。
9. 插件依赖排序、生命周期、失败隔离符合 Spigot / MoBoxPanel 风格。
10. 插件能注册事件监听器和聊天命令。
11. 命令支持权限、别名、冷却和参数解析。
12. `status` 与 `plugin list` 能查看连接、心跳、插件状态。
13. 配置错误、协议错误和插件异常不能导致主进程崩溃。
14. SQLite 能持久化插件记录、群配置与权限数据。
15. 关闭进程时插件资源、线程和连接能正确释放。
16. 不包含 Web 管理模块，Web 管理保留到 M9。

## 16. 决策状态

已确认：

| 项目 | 决策 |
|---|---|
| Java 版本 | Java 8 |
| 包名 | `org.moboxlab.moboxbot` |
| 构建方式 | `build.ps1` + `javac` + `out/` fat jar |
| 基础库 | 复用 `MossLib.jar` |
| WebSocket 库 | Java-WebSocket |
| 数据存储 | SQLite |
| Web 管理 | 暂不做，M9 再评估 |
| 插件内核 | Spigot 式自研 |
| 默认连接模式 | 正向 WebSocket |
| 插件热重载 | 只在开发模式启用 |
| 版本规则 | 沿用 MoBoxPanel |
| 代码风格 | 参考 MoBoxPanel + `my-code-style` |

M0 已完成：

| 项目 | 结果 |
|---|---|
| MossLib 文档 | 已读 `MoBoxPanel/MossLib-APIDOC.md` |
| NapCat 文档 | 已拉取 `_ref/NapCatDocs` 与 `_ref/napneko.github.io` |
| NapCat 源码 | 已拉取 `_ref/NapCatQQ` |
| Java-WebSocket 版本 | 固定 `1.6.0` |

M0 仍需实测：

| 验证项 | 目标 |
|---|---|
| MossLib 空跑 | 用 Java 8 编译并启动，验证日志、配置、命令、SQLite 四项 |
| NapCat 正向 WS | 按 `websocketServers` 配置实际连接一次 |
| SQLite 建表释放 | 确认首次运行、升级、旧库补列行为 |

## 17. 下一步

1. 按 NapCat 文档整理 OneBot 11 覆盖清单与事件字段表。
2. 整理 MoBoxBot 自己的 `STYLE.md`。
3. 整理 MoBoxBot 自己的 `PLUGIN.md`。
4. 输出 `API` 包接口草案。
5. 输出 `config.yml` 与 `struct-sqlite.sql` 草案。
6. 再进入 M1 工程骨架实现。

## 18. 参考资料

| 资料 | 路径 | 说明 |
|---|---|---|
| MoBoxPanel 计划与插件规范 | `D:\CodeX\Projects\MoBoxPanel\PLUGIN.md` | Spigot 式插件 API 的直接参考 |
| MoBoxPanel 代码风格 | `D:\CodeX\Projects\MoBoxPanel\STYLE.md` | Java 8 与项目调用习惯 |
| MossLib 能力与踩坑 | `D:\CodeX\Projects\MoBoxPanel\MossLib-APIDOC.md` | MossLib 复用结论 |
| NapCat 源码 | `_ref/NapCatQQ` | 协议端实现参考 |
| NapCat 文档源 | `_ref/NapCatDocs` | OneBot 网络、事件、消息段、API 文档 |
| NapCat 文档站 | `_ref/napneko.github.io` | 构建后的静态站点 |
| Java-WebSocket 1.6.0 | `_ref/Java-WebSocket-1.6.0` | 最终 WebSocket 依赖参考 |
| Java-WebSocket 1.5.7 | `_ref/Java-WebSocket-1.5.7` | 对比参考，不作为最终依赖 |
