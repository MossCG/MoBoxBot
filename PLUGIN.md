# PLUGIN.md — MoBoxBot 插件开发规范

> 适用：MoBoxBot 主程序插件。
> 插件 API 版本：**0.1（M0 草案）**。
> 参考：MoBoxPanel 的 `PLUGIN.md`、`_ref/NapCatDocs`、[API.md](API.md)。

## 1. 插件是什么

插件是一个 JAR 包，放在运行目录的 `./MoBoxBot/plugins/` 下。JAR 根目录必须有 `plugin.json`，主类必须继承 `org.moboxlab.moboxbot.API.Plugin`。

```text
./MoBoxBot/plugins/
├─ MoBoxBot-ExamplePlugin.jar
└─ MoBoxBot-ExamplePlugin/
   └─ config.yml
```

加载流程：

```text
扫描 plugins/*.jar
  → 读 plugin.json
  → 校验 name / main / apiVersion
  → 依赖校验与拓扑排序
  → 每个插件创建一个 PluginClassLoader
  → 实例化 main 类
  → 建插件数据目录
  → onLoad()
  → onEnable()：依赖已就绪，注册监听器、命令、任务
  → 写 bot_plugin_record
```

三条硬规矩：

1. 单个插件失败绝不拖累主程序启动。
2. 插件注册的一切资源都由主程序按插件记账，停用或重载时统一回收。
3. 插件只能依赖 `org.moboxlab.moboxbot.API`，不能碰主程序内部类。

## 2. plugin.json

```json
{
  "name": "MoBoxBot-ExamplePlugin",
  "version": "V0.0.1.0.0000",
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
| `name` | 是 | 唯一标识，同时是数据目录名与依赖声明名 |
| `version` | 否 | 插件版本，沿用主程序版本规则 |
| `apiVersion` | 是 | 依赖的插件 API 版本，主版本必须一致 |
| `main` | 是 | 主类全限定名，必须继承 `API.Plugin` |
| `author` / `description` / `website` | 否 | 展示用 |
| `depend` | 否 | 强依赖：缺失或加载失败则本插件不加载 |
| `softDepend` | 否 | 弱依赖：存在则先加载，不存在也继续 |
| `loadBefore` | 否 | 反向声明：希望在某个插件之前加载 |
| `provides` | 否 | 本插件提供的功能标识 |

## 3. 生命周期

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
        getLogger().sendInfo("示例插件已停用！");
    }
}
```

硬规矩：

- `onLoad` 阶段不要访问其他插件。
- `onEnable` 阶段依赖已全部就绪。
- 插件不要把异常抛给主程序。失败时自己 `getLogger().sendWarn(...)` 并返回 `null` / `false`。
- `onDisable` 只释放插件自己创建的资源，例如线程、连接、临时文件。

## 4. 类加载与隔离

每个插件一个 `PluginClassLoader`，继承 `URLClassLoader`，策略与 MoBoxPanel 一致：

| 包 | 由谁加载 | 原因 |
|---|---|---|
| `java.*` / `javax.*` / `com.sun.*` | 父加载器 | JRE |
| `com.alibaba.fastjson.*` | 父加载器 | 保证插件与主程序是同一个 `JSONObject` 类 |
| `org.moboxlab.moboxbot.API.*` | 父加载器 | 接口只有一个定义 |
| 主程序其他包 | 父加载器 | 宽松放开，约定插件不要用 |
| 插件自己的包 | 插件加载器 | 插件 JAR |

注意：

1. 插件 JAR 不要打包 MoBoxLib、fastjson、sqlite-jdbc、Java-WebSocket。
2. 插件读自己的资源用 `readResource` / `readResourceText`，不要用 `getResourceAsStream`。
3. 插件线程、连接、定时任务必须在 `onDisable` 中释放。
4. 生产环境更新插件建议重启进程，热重载只保证主程序不重启。

## 5. API 速查

### 5.1 入口

| 调用 | 说明 |
|---|---|
| `getServer()` | 主程序门面 |
| `getLogger()` | 插件日志，自动带 `[插件名]` 前缀 |
| `getConfig()` | 插件配置 |
| `getDataFolder()` | 插件数据目录 |
| `getName()` / `getVersion()` / `getDescription()` | 插件元数据 |
| `readResource` / `readResourceText` | 读插件 JAR 里的资源 |
| `MoBoxBotAPI.getServer()` / `getVersion()` / `getApiVersion()` | 静态入口 |

### 5.2 服务门面

```java
getServer().getVersion();
getServer().getApiVersion();
getServer().getBotName();
getServer().getPluginManager();
getServer().getEventBus();
getServer().getOneBotClient();
getServer().getStorage();
```

### 5.3 插件管理器

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

### 5.4 OneBot 客户端

| 方法 | 用途 |
|---|---|
| `sendGroupMessage(groupID,message)` | 发群消息 |
| `sendPrivateMessage(userID,message)` | 发私聊消息 |
| `deleteMessage(messageID)` | 撤回消息 |
| `getGroupList()` | 群列表 |
| `getGroupMemberInfo(groupID,userID)` | 群成员信息 |
| `setGroupBan(groupID,userID,duration)` | 群禁言 |
| `callAction(action,params)` | 调用扩展 API |

消息统一用 fastjson：

```java
JSONArray message = MessageUtil.text("pong");
OneBotClient.sendGroupMessage(groupID,message);
```

## 6. 事件

监听器实现 `API.Event.Listener`，方法上加 `@EventHandler`：

```java
public class ExampleListener implements Listener {
    private final ExamplePlugin plugin;

    public ExampleListener(ExamplePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGroupMessage(GroupMessageEvent event) {
        plugin.getLogger().sendInfo("收到群消息：" + event.getGroupID());
    }
}
```

注册：

```java
getServer().getPluginManager().registerListener(this,new ExampleListener(this));
```

优先级：

| 优先级 | 用途 |
|---|---|
| `LOWEST` | 最早观察或预处理 |
| `LOW` | 提前处理 |
| `NORMAL` | 默认 |
| `HIGH` | 后置处理 |
| `HIGHEST` | 最终处理 |
| `MONITOR` | 只观察记录，不改业务数据 |

规则：

- 分发默认同步，保证顺序语义。
- 耗时操作用 `getEventBus().callEventAsync(event)` 或 `runTask`。
- 监听器抛异常只记日志，不影响其他监听器。
- `MONITOR` 不要修改事件或业务数据。

首版事件类草案见 [API.md](API.md)。

## 7. 聊天命令

命令继承 `API.Command.BotCommand`：

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

注册：

```java
getServer().getPluginManager().registerCommand(this,new PingCommand());
```

命令路由：

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

## 8. 插件配置

插件配置放在：

```text
./MoBoxBot/plugins/<插件名>/config.yml
```

```java
@Override
public void onLoad() {
    saveDefaultConfig();
    String prefix = getConfig().getString("commandPrefix","/");
}
```

配置只认 `键: 值` 一层结构。写回时逐行替换，保留注释、空行、键顺序与换行风格。布尔与整数裸写，其余加双引号。

## 9. 数据存储

小数据优先用 `getServer().getStorage()`：

| 方法 | 用途 |
|---|---|
| `get(plugin,key)` | 读键值 |
| `set(plugin,key,value)` | 写键值 |
| `remove(plugin,key)` | 删除键值 |
| `query(sql,params)` | 查询 |
| `update(sql,params)` | 增删改 |
| `insert(sql,params)` | 插入 |

结构化数据由插件自己建表，表名统一：

```text
plugin_<插件名>_
```

插件名先转小写，非 `a-z0-9` 字符替换为下划线。例如：

```text
MoBoxBot-ExamplePlugin
  → plugin_moboxbot_exampleplugin_
```

SQL 规则与主程序一致：

- 一律 `PreparedStatement` + `?`。
- 列名加反引号。
- 不拼用户输入。
- 事务短小，避免 SQLite 写锁冲突。

## 10. 热重载与资源回收

热重载只在开发模式启用：

```yaml
pluginHotReload: false
pluginDevMode: false
```

开发模式可以：

```text
plugin reload MoBoxBot-ExamplePlugin
```

重载流程：

```text
onDisable()
  → 回收该插件登记的监听器、命令、任务、存储句柄
  → 关闭旧类加载器
  → 重新加载并启用
```

边界：

1. 只保证主程序不重启，不保证插件自身无泄漏。
2. 插件自己开的线程、连接池、定时器要自己关。
3. 生产环境更新插件建议重启进程。
4. 插件注册项不能与主程序或其他插件重复。

## 11. 示例插件结构

```text
MoBoxBot-ExamplePlugin/
├─ build.ps1
├─ src/main/java/org/moboxlab/example/
│  ├─ ExamplePlugin.java
│  ├─ Listener/
│  │  └─ ExampleListener.java
│  └─ Command/
│     └─ PingCommand.java
└─ src/main/resources/
   ├─ plugin.json
   └─ config.yml
```

编译时只依赖主程序 JAR：

```powershell
javac -encoding UTF-8 -cp "MoBoxBot/out/MoBoxBot.jar" -d classes <你的java>
```

## 12. 开发自检

1. JAR 根目录必须有 `plugin.json`。
2. 主类必须继承 `API.Plugin`。
3. 插件 JAR 里不要有 MoBoxLib、fastjson、sqlite-jdbc、Java-WebSocket。
4. 把 JAR 放进 `./MoBoxBot/plugins/`，重启或开发模式重载。
5. 故意写错一个依赖或让构造函数抛异常，确认只有该插件失败。
6. 停用后确认监听器、命令、任务都回收。
7. 重载后确认没有旧监听器继续响应。

## 13. 插件 API 版本

| API 版本 | 说明 |
|---|---|
| `0.1` | M0 草案：插件基类、事件、命令、配置、OneBot 客户端、存储接口 |

破坏性变更必须进位 API 次版本，并同步 [API.md](API.md) 与示例插件。

## 14. 参考

- [API.md](API.md)：插件 API 草案。
- [STYLE.md](STYLE.md)：代码风格。
- [ONEBOT.md](ONEBOT.md)：OneBot 11 覆盖清单。
- `D:\CodeX\Projects\MoBoxPanel\PLUGIN.md`：Spigot 式插件内核参考。
