# AGENTS.md

## 前置约束（每次动手前必读）

- 先读 [STYLE.md](STYLE.md)、[PLUGIN.md](PLUGIN.md)、[PLAN.md](PLAN.md)。
- 技术栈锁定：Java 8 + MoBoxLib + fastjson + Java-WebSocket 1.6.0 + SQLite。
- 保持一致比“更现代”更重要。新增功能先找 MoBoxPanel / MoBoxBot 同类文件照抄结构与注释密度。
- 中文注释、中文日志、中文配置说明；不用 emoji。
- 当前项目不做 Web 管理；M9 再评估，届时另读 `mobox-ui-style`。
- 官方插件示例仓库：`https://github.com/MossCG/MBB-ExamplePlugin`。

## 当前阶段

- M0 文档阶段已完成。
- M1-M7 已完成：工程骨架、SQLite、OneBot、事件、插件内核、命令权限、示例插件。
- 当前进入 M8：Mock OneBot、真实 NapCat 端到端、部署文档与构建增强。

## 安全底线

- 仓库里只放代码与文档。运行目录 `./MoBoxBot/`、构建产物 `out/`、日志、数据库、真实凭据都在 `.gitignore` 里。
- `_ref/` 只作本地参考资料，永远不提交。
- 配置模板里的密钥类字段一律留空，例如 `oneBotToken`、数据库密码、第三方 API Key。
- 文档里不写活凭据。测试账号可以写，密码、Token、Cookie、密钥一律不写。
- 提交前检查被跟踪文件：

```powershell
git grep -n -I -E "oneBotToken\s*[:=]\s*[`"'][^`"']{8,}|token\s*[:=]\s*[`"'][^`"']{8,}|password\s*[:=]\s*[`"'][^`"']{6,}"
```

命中的要么是字段名，要么就是真漏了。真漏了先脱敏再提交，并提醒用户轮换对应凭据。

## 版本与更新日志

版本号格式：`V大版本.小版本.小更新.小修正.四位时间戳`，例如 `V0.0.1.0.1930`。

开发阶段大版本固定为 `0`，正式发布才进位到 `1`。日常功能更新走小版本 / 小更新 / 小修正。

| 位 | 含义 | 何时进位 |
|---|---|---|
| 大版本 | 正式推送 | 整体对外发布时进位 |
| 小版本 | 大功能更新 | 新模块、接口破坏性变更 |
| 小更新 | 常规更新 | 功能完善、BUG 修正 |
| 小修正 | 极小修正 | 拼写、文案、格式类修正 |
| 四位时间戳 | 记录时刻 | 24 小时制四位，8:21 记 `0821`，19:15 记 `1915` |

提交与日志规则：

1. 代码 + 文档同一轮更新，按版本号提交：`V0.0.1.0.1930 项目初始化：建立骨架与构建脚本`。
2. 纯文档调整用：`docs: 中文摘要`。
3. 仓库配置、忽略规则等非功能调整用：`chore: 中文摘要`。
4. 每轮功能完成后追加 [update.md](update.md)，并同步 `BasicInfo.version`。
5. 一轮内容较多时可以拆分提交，但同一功能不要拆成互相依赖的碎片提交。
6. M0 属于文档阶段，不占版本号；M1 开始产生第一个 `V0...` 版本。
7. 主程序版本只跟随 `MoBoxBot` 主仓库的代码变更。插件仓库 `D:\CodeX\Projects\MBB-Plugins` 的插件改动不要求主程序更新版本号，除非同时改动了主程序 API 或核心代码。

## 仓库拆分

本项目由两个独立 Git 仓库组成：

| 仓库 | 路径 | 说明 |
|---|---|---|
| 主程序 | `D:\CodeX\Projects\MoBoxBot` | NapCat OneBot 客户端、插件内核、API、文档 |
| 插件 | `D:\CodeX\Projects\MBB-Plugins` | MBB-* 独立插件源码与构建脚本 |
| 官方示例 | `D:\CodeX\Projects\MBB-ExamplePlugin` | API 官方插件开发示例 |

插件仓库有自己的提交记录和版本节奏。插件改动只改插件仓库；主程序 API 变更才需要同步更新主程序版本。

## 目录与提交边界

```text
MoBoxBot/
├─ AGENTS.md
├─ STYLE.md
├─ PLUGIN.md
├─ PLAN.md
├─ API.md
├─ ONEBOT.md
├─ update.md
├─ build.ps1
├─ run.bat
├─ depend/
├─ src/main/java/org/moboxlab/moboxbot/
├─ src/main/resources/
├─ src/test/java/
├─ out/
└─ _ref/                    # 本地参考资料，不提交
```

- `depend/` 放 `MossLib.jar` 与 `Java-WebSocket-1.6.0.jar`，这两个 jar 属于源码依赖，允许提交。
- `out/`、`./MoBoxBot/`、`logs/`、`*.db` 不提交。
- `_ref/` 里的 NapCat 与 Java-WebSocket 仓库不提交。

## 插件 API 变更

- 插件 API 版本在 `org.moboxlab.moboxbot.API.MoBoxBotAPI.API_VERSION`。
- 当前 API 0.3 已冻结，形状变更必须同步 `ApiFreezeTest`、[API.md](API.md) 与官方示例。
- 破坏性变更必须进位 API 次版本，并同步 [PLUGIN.md](PLUGIN.md)、[API.md](API.md) 与示例插件。
- 插件只允许依赖 `org.moboxlab.moboxbot.API`。
- 插件 JAR 不允许打包 MossLib、fastjson、sqlite-jdbc、Java-WebSocket。

## 交付前自检

1. `javac -encoding UTF-8 -cp "depend/MossLib.jar;depend/Java-WebSocket-1.6.0.jar" -d <临时目录> <全部 java>` 必须通过。
2. `build.ps1` 必须能产出 `out/MoBoxBot.jar`。
3. 插件加载测试必须覆盖：正常加载、缺依赖、循环依赖、构造函数异常、停用回收、重载。
4. 全库复查无旧包名、旧品牌词、旧运行目录残留。
5. 提醒部署方：`config.yml` 与 `struct-sqlite.sql` 只在文件不存在时释放，升级需按文档处理。
6. 产物 JAR 必须重新构建，不能使用旧缓存。
