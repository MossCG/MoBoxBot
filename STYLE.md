# STYLE.md — MoBoxBot 代码风格与调用习惯

> 来源：MoBoxPanel 的 `STYLE.md`、`my-code-style` 技能与 MoBoxBot 计划书。
> 适用范围：MoBoxBot 主程序与 MoBoxBot 插件。
> 核心原则：保持一致比“更现代”更重要。

## 0. 三条底线

1. 技术栈锁定：Java 8 + MoBoxLib + fastjson + Java-WebSocket 1.6.0 + SQLite，不引入 Spring、Lombok、PF4J、MyBatis。
2. 风格跟着同类文件走，动手前先读一个同类文件，照抄它的结构、注释密度与命名。
3. 中文注释、中文日志、中文配置说明，不用 emoji。

## 1. 工程骨架

### 1.1 包结构

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

插件自己的包名由插件作者决定，例如 `org.moboxlab.example`。

### 1.2 命名规律

| 类型 | 命名 | 示例 |
|---|---|---|
| 模块入口 | `XxxMain` | `Main`、`OneBotMain`、`DatabaseMain` |
| 插件接口 | `PluginXxx` | `PluginManager`、`PluginDescription`、`PluginState` |
| 插件实现 | `PluginXxxImpl` | `PluginManagerImpl` |
| 事件类 | `XxxEvent` | `GroupMessageEvent`、`BotOnlineEvent` |
| 控制台命令 | `CommandXxx` | `CommandStatus`、`CommandPlugin` |
| 工具类 | `XxxUtil` / `Xxx` | `MessageUtil`、`TextUtil` |

### 1.3 运行目录

运行目录固定 `./MoBoxBot/`：

```text
./MoBoxBot/
├─ config.yml
├─ data/
│  └─ bot.db
├─ logs/
├─ plugins/
└─ dependency/
```

启动时用 `FileCheck.checkDirExist` / `FileCheck.checkFileExist` 从 JAR 释放模板文件。多级目录必须逐级创建。

## 2. Java 写法

### 2.1 全静态、零依赖注入

- 业务工具类全部静态：没有构造器、不写接口与抽象类。
- 只有插件 API、事件模型、框架必须扩展的地方允许接口与抽象类。
- 跨模块调用一律 `XxxClass.method(...)` 静态调用。
- 需要“每类私有”的用 `private static final`。
- 不做依赖注入，不引入 Spring 容器。

### 2.2 全局状态集中在 BasicInfo

```java
public class BasicInfo {
    public static String version = "V0.0.1.0.0000";
    public static String author = "MossCG";
    public static final String runDir = "./MoBoxBot";

    public static ObjectLogger logger;
    public static ObjectConfig config;
    public static PluginManager pluginManager;
    public static OneBotClient oneBotClient;
    public static boolean debug = false;
    public static long startTime = 0L;

    public static void sendDebug(String message) {
        logger.sendAPI(message,debug);
    }
}
```

字段旁写一行中文注释说明生命周期。插件不得直接访问 `BasicInfo`，只能通过 `API.Server` 门面。

### 2.3 命名与排版

- 类 `PascalCase`，方法 / 字段 `camelCase`，包内子目录 `PascalCase`。
- 4 空格缩进，K&R 大括号，方法之间不留分隔线。
- 方法内用分节注释分段，例如 `//配置读取`、`//数据库操作`、`//事件分发`。
- 逗号后不加空格：`update(data,responseData);`。
- Java 字符串拼接 `+` 两侧不加空格：`"启动完成！耗时："+(completeTime-startTime)+"毫秒！"`。
- for 循环头带空格：`for (int i = 0; i < list.size(); i++)`。
- 通配导入最多用于同类集合，其余显式导入。

### 2.4 Java 8 且克制

- 不用 `var`、不用 `.stream()`、不用 Lombok、不用 `Optional`。
- `List` / `Map` / `String[]` 显式声明类型。
- 简单校验直接一行：`if (data == null) return;`。
- lambda 只用于任务注册、forEach 这类短逻辑，复杂逻辑写普通方法。

### 2.5 异常与日志

```java
} catch (Exception e) {
    BasicInfo.logger.sendException(e);
}
```

四级日志分工：

| 方法 | 用途 |
|---|---|
| `sendInfo` | 正常流程节点 |
| `sendWarn` | 可恢复问题、参数错误、降级 |
| `sendException` | 捕到的异常，`sendException(e)` 不带文案 |
| `sendDebug` | 受 `BasicInfo.debug` 开关控制的接口与事件流水 |

硬规则：

- 零 `System.out`，零 `printStackTrace`。
- 日志文案口语化、带感叹号，允许“哦~”“喵”这类语气。
- 插件异常只能记日志，不能拖垮主程序。

## 3. 各层模板

### 3.1 Main 类

Main 只干三件事：按顺序装配模块、输出启动日志、提供热重载入口。

装配顺序：

```text
运行目录
  → 日志
  → 依赖
  → 版本信息
  → 配置读取
  → 开关检查
  → SQLite
  → 定时任务框架
  → 插件加载与启用
  → OneBot 连接
  → 控制台命令
  → 启动完成耗时
```

硬规矩：

1. 每加一个模块，就在 `Main` 里加一段“分节注释 + 初始化调用”。
2. 后台线程和定时任务一律在 `Main` 里显式启动，不在静态块里偷偷 start。
3. `reloadConfig()` 是唯一热重载入口，需要热生效的缓存清理都写进这里。

### 3.2 控制台命令

```java
public class CommandStatus extends ObjectCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("status");
        prefixList.add("stat");
        return prefixList;
    }

    @Override
    public boolean execute(String[] args,ObjectLogger logger) {
        BasicInfo.logger.sendInfo("MoBoxBot 状态：" + OneBotMain.getStateText());
        return true;
    }
}
```

新命令要在 `Main` 里注册。

### 3.3 OneBot Action 调用

```java
//消息处理
JSONArray message = MessageUtil.text("pong");
JSONObject result = OneBotClient.sendGroupMessage(groupID,message);
if (result == null || result.getIntValue("retcode") != 0) {
    BasicInfo.logger.sendWarn("群消息发送失败：" + groupID);
    return;
}
```

- 业务代码不直接拼 WebSocket JSON。
- 主动调用统一走 `OneBotClient`。
- 发送类接口要经过队列与频率限制。

### 3.4 数据库

```java
//数据库操作
DatabaseMain.checkDatabase();
Connection connection = DatabaseMain.getConnection();
PreparedStatement statement = connection.prepareStatement("SELECT * FROM `bot_group_config` WHERE `groupID`=?");
statement.setString(1,groupID);
ResultSet set = statement.executeQuery();
if (set.next()) {
    //读取字段
}
```

规则：

- 一律 `PreparedStatement` + `?` 占位符。
- 列名加反引号。
- 动态条件用 `StringBuilder` 拼 `WHERE 1=1`，不拼用户输入。
- 分页统一 `page` / `pageSize`，`pageSize` 有上限。
- 所有 SQL 走 `SqlExecutor`，业务代码不直接持有连接。

### 3.5 插件实现

插件只依赖 `API` 包：

```java
public class ExamplePlugin extends Plugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerListener(this,new ExampleListener(this));
        getServer().getPluginManager().registerCommand(this,new ExampleCommand());
    }

    @Override
    public void onDisable() {
        getLogger().sendInfo("示例插件已停用！");
    }
}
```

插件禁止：

- 直接访问 `BasicInfo`。
- 直接访问 `Database`、`OneBot` 内部类。
- 使用 `getResourceAsStream` 读插件资源。
- 打包 MoBoxLib、fastjson、sqlite-jdbc、Java-WebSocket。

### 3.6 配置写回

凡是写回用户配置文件，都必须逐行替换，保留注释、空行、键顺序与换行风格：

```java
public static synchronized boolean writeConfig(String key,String value) {
    //只修改文件中已存在的键
    //写回时保留行尾 \r，值按原风格格式化
    return true;
}
```

值格式化：

- 布尔与整数裸写。
- 其余内容加双引号。
- 转义 `\` 与 `"`。
- 读改写必须幂等，第二次写入不应再产生变化。

## 4. 调用习惯

### 4.1 新增控制台命令

1. `Command/` 新建 `CommandXxx.java`，实现 `prefix()` 与 `execute()`。
2. `Main.main()` 的命令初始化段落里注册。
3. 命令内部调用已有静态方法，不重复实现业务。

### 4.2 新增配置

1. `src/main/resources/config.yml` 对应段落加键 + 中文注释。
2. 代码里用 `BasicInfo.getConfigString/getConfigInt/getConfigBoolean` 读取。
3. 需要热生效的，在 `Main.reloadConfig()` 里补缓存刷新。
4. 必须重启生效的，在文档里注明。

### 4.3 新增 OneBot Action

1. 在 `OneBotClient` 增加静态方法。
2. 参数用 camelCase 的 Java 参数，内部转 OneBot snake_case 字段。
3. 返回 `JSONObject`，保留 `status`、`retcode`、`data`。
4. 发送类接口接入队列与频率限制。
5. 更新 [ONEBOT.md](ONEBOT.md) 覆盖表。

### 4.4 新增插件 API

1. 在 `API` 包增加接口或事件类。
2. 在 `PLUGIN.md` 与 `API.md` 补说明。
3. 破坏性变更必须进位 `API_VERSION`。
4. 核心实现只允许出现在 `Plugin` / `OneBot` / `Database` 包。

### 4.5 固定调用链

| 场景 | 固定写法 |
|---|---|
| 取配置 | `BasicInfo.getConfigString("key","default")` |
| 记录正常流程 | `BasicInfo.logger.sendInfo("正在……")` |
| 记录异常 | `BasicInfo.logger.sendException(e)` |
| 记录调试流水 | `BasicInfo.sendDebug("...")` |
| 发消息 | `OneBotClient.sendGroupMessage(groupID,message)` |
| 查数据库 | `SqlExecutor.query(...)` |
| 配置热重载 | `Main.reloadConfig()` |
| 插件注册监听器 | `getServer().getPluginManager().registerListener(this,listener)` |
| 插件注册命令 | `getServer().getPluginManager().registerCommand(this,command)` |

## 5. 前端与 Web

MoBoxBot 暂不做 Web 管理。M9 如果启动 Web 控制台，再引入 `mobox-ui-style` 与 MoBoxPanel 的前端规范。本阶段不写页面、不引前端依赖。

## 6. 交付前自检

1. `javac -encoding UTF-8 -cp "depend/MoBoxLib.jar;depend/Java-WebSocket-1.6.0.jar" -d <临时目录> <全部 java>` 编译通过。
2. `build.ps1` 能产出 `out/MoBoxBot.jar`。
3. 插件加载、启用、停用、重载、异常隔离全部跑过。
4. 配置写回幂等，注释与顺序未丢。
5. SQLite 新建、升级、旧库补列都验证过。
6. 全库复查无旧包名、旧运行目录、旧品牌词。

## 7. 参考

- [PLAN.md](PLAN.md)：项目计划与决策。
- [PLUGIN.md](PLUGIN.md)：插件开发规范。
- [API.md](API.md)：插件 API 草案。
- [ONEBOT.md](ONEBOT.md)：OneBot 11 覆盖清单。
- `D:\CodeX\Projects\MoBoxPanel\STYLE.md`：同源项目风格依据。
- `D:\CodeX\Projects\MoBoxPanel\PLUGIN.md`：插件内核实现参考。
- `_ref/NapCatDocs`：NapCat 与 OneBot 11 文档。
