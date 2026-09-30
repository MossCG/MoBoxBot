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
