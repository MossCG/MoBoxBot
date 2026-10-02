# update.md — 更新日志

## 版本号规则

格式：`V大版本.小版本.小更新.小修正.四位时间戳`，例如 `V0.0.1.0.1930`。

开发阶段大版本固定为 `0`，正式发布才进位到 `1`。

| 位 | 含义 | 何时进位 |
|---|---|---|
| 大版本 | 正式推送 | 整体对外发布时进位 |
| 小版本 | 大功能更新 | 新模块、接口破坏性变更 |
| 小更新 | 常规更新 | 功能完善、BUG 修正 |
| 小修正 | 极小修正 | 拼写、文案、格式类修正 |
| 四位时间戳 | 记录时刻 | 24 小时制四位，8:21 记 `0821`，19:15 记 `1915` |

进位规则：低位进位后，其后的低位清零。例如 `V0.0.3.2.0930` 升小版本为 `V0.1.0.0.1625`。

流程：每轮功能更新完成后按内容定级，生成新版本号，写入本文件，并同步 `BasicInfo.version`。纯文档调整用 `docs:` 提交，不占版本号。

## 更新记录

| 版本号 | 日期 | 级别 | 更新内容 |
|---|---|---|---|
| M0 | 2026-09-30 | 文档阶段 | 初始化 MoBoxBot 仓库、计划书、协作规范、代码风格、插件规范、插件 API 草案、OneBot 11 覆盖清单、`config.yml` 与 `struct-sqlite.sql` 草案；确定 Java 8、MossLib、Java-WebSocket 1.6.0、正向 WebSocket、SQLite、`org.moboxlab.moboxbot`、Spigot 式插件内核 |
| V0.1.0.0.2030 | 2026-09-30 | 小版本 | **M1-M7 核心实现**：`MoBoxLib` 精简基础库（Apache-2.0，移除 MySQL/JavaMail/OSHI/JNA，修复取连接 sleep 200ms、YAML 读取、目录创建和配置空值问题）；MoBoxBot 主程序骨架、Java 8 构建脚本、控制台命令、SQLite `DatabaseMain`/`SqlExecutor`/建表初始化；OneBot 11 正向/反向 WebSocket、Token 鉴权、心跳检测、重连、echo Action 调用、群聊/私聊/通知/请求/元事件解析；Spigot 式插件内核（`plugin.json`、依赖排序、独立类加载器、生命周期、资源记账回收、`@EventHandler` 事件总线、聊天命令、权限与冷却、插件数据存储）；示例插件 `MoBoxBot-ExamplePlugin` 与轻量测试入口 |
| V0.1.1.0.2034 | 2026-09-30 | 小更新 | **MoBoxLib 依赖升级**：MoBoxLib 改用 fastjson 1.2.83、SQLite JDBC 3.45.3.0、SnakeYAML 2.2、Log4j 2.20.0、SLF4J API/NOP 1.7.36；不再从 MossLib fat jar 提取旧版依赖，避免 fastjson 1.2.78、SnakeYAML 1.33、SQLite 3.34.0 的旧版本问题；MoBoxLib 冒烟测试与 MoBoxBot `TestMain` 重新通过 |
| V0.1.2.0.1715 | 2026-10-01 | 小更新 | **管理命令与编码修复**：示例插件 `ping` 改为 `BOT_ADMIN` 权限，新增示例管理员命令 `hello`；新增内置聊天命令 `plugins`，仅 `BOT_ADMIN` 及以上可用，显示插件名称、版本、状态、监听/命令/任务数量；MoBoxLib 控制台日志改用 UTF-8 输出，`run.bat` 增加 `chcp 65001`、工作目录切换和 `-Dfile.encoding=UTF-8`，解决 Windows 控制台乱码；新增使用文档 |
| V0.1.3.0.1747 | 2026-10-01 | 小更新 | **配置注释与多管理员说明**：`config.yml` 每一项都补充中文注释；明确 `botAdmin` 支持多个管理员 QQ 号，必须使用英文逗号分隔；使用文档同步补充多管理员示例 |
| V0.1.4.0.2228 | 2026-10-01 | 小更新 | **插件化命令调整**：移除主程序内置 `/plugins` 聊天命令，改由独立插件 `MBB-Plugins` 提供，权限为 `OWNER`；为 `MBB-Ping`、`MBB-Plugins`、`MBB-Status` 三个独立插件工程预留接口；使用文档同步更新插件命令来源和权限说明 |
| V0.2.0.0.2256 | 2026-10-01 | 小版本 | **图片命令与闭麦体系**：新增 `ImageUtil` 图片渲染 API 与 `CommandSender.sendImage`；新增内置闭麦服务，`/quiet`（别名 `/muteself`、`/selfmute`）仅 `BOT_ADMIN` 可用，闭麦后不响应任何消息、命令、通知和戳一戳，只接受下一次管理员解除命令；新增独立插件 `MBB-Help`、`MBB-Version`、`MBB-Reload`、`MBB-Remind`、`MBB-Random`、`MBB-Poke`；`MBB-Plugins` 改为图片展示插件详情；`Server` 增加 `reloadConfig()`；`run.bat` 增加 headless 参数；API 与使用文档同步更新 |
| V0.3.0.0.2312 | 2026-10-01 | 小版本 | **金色猫娘图片与权限体系**：`ImageUtil` 改为金色猫娘主题，右下角接入本地 ComfyUI 生成的角色图；命令支持 `@机器人 /命令` 和 `@机器人 命令`；新增 `CommandInfo` 与 `Server.getCommandList()`，`/help` 按权限分区显示当前用户可用命令；新增 `ConfigEditor`、`AdminService` 与 `Server` 管理员管理接口，owner 可通过 `MBB-Admin` 插件执行 `/admin list/add/remove`；同步更新 API、使用文档和第三方声明 |
| V0.3.1.0.2345 | 2026-10-01 | 小更新 | **多 owner、浅色图片与 debug 日志**：`botOwner` 支持多个 QQ，英文逗号分隔；移除猫娘角色图，`ImageUtil` 改为更浅的金色配色；开启 `debug` 后控制台显示 OneBot WS 收到的全部原始内容，便于排查事件和 Action 响应 |
| V0.4.0.0.0048 | 2026-10-02 | 小版本 | **插件管理、Welcome、Poll、PigHub**：`MBB-Plugins` 增加 enable/disable/reload/info，图片显示版本 By 作者、命令/监听/任务数量，禁止停用或重载自身；新增 `MBB-Welcome` 进群退群猫娘欢迎语和群级开关；新增 `MBB-Poll` 限时投票与结果 @ 发起者；新增 `MBB-PigHub` 关键词“来只猪猪”随机发送 PigHub 图片；新增 `PluginInfo` 与 `Server.getPluginInfoList()` |
| V0.5.0.0.0201 | 2026-10-02 | 小版本 | **消息日志、API 冻结与官方示例独立**：`enableMessageLog` 开启后控制台记录群聊、私聊、通知、请求、元事件和机器人发送内容，图片显示为 `[图片]`；API 0.1 完成审计、文档冻结并加入构建期 `ApiFreezeTest`；旧 `example-plugin` 移出主仓库，改为独立 `MBB-ExamplePlugin` 官方示例仓库并在开发协作区登记 |
| V0.5.0.1.0233 | 2026-10-02 | 小修正 | 消息日志不再显示 `meta_event.heartbeat`，避免心跳事件持续刷屏 |
| V0.6.0.0.2240 | 2026-10-02 | 小版本 | 插件 API 升级到 `0.2`，新增通用 `PluginService` 服务注册表，插件可按名称注册和调用公共服务，停用与重载时自动回收 |
| V0.7.0.0.0013 | 2026-10-03 | 小版本 | 插件 API 升级到 `0.3`，新增 `BotCommand.usage()` 与 `CommandInfo.usages`，帮助插件可以展示插件的子命令和参数用法 |
