# MoBoxBot 使用文档

## 1. 环境要求

- Java 8
- NapCatQQ
- NTQQ 客户端
- Windows 或 Linux

MoBoxBot 默认使用正向 WebSocket 连接 NapCat：

```text
MoBoxBot → ws://127.0.0.1:3001
```

## 2. 目录结构

```text
MoBoxBot/
├─ MoBoxBot.jar
├─ run.bat
├─ MoBoxBot/
│  ├─ config.yml
│  ├─ data/
│  ├─ logs/
│  ├─ plugins/
│  └─ dependency/
└─ NapCat/
   └─ NapCatShell/
```

`run.bat` 必须在 MoBoxBot.jar 所在目录运行。脚本会自动切换工作目录，运行目录固定为同级的 `./MoBoxBot/`。

## 3. 构建

Windows：

```powershell
.\build.ps1
```

产物：

```text
out\MoBoxBot.jar
```

## 4. 配置

首次运行会释放：

```text
./MoBoxBot/config.yml
```

常用配置：

```yaml
enable: true
botName: "MoBoxBot"
botOwner: "所有者QQ号1,所有者QQ号2"
botAdmin: "管理员QQ号1,管理员QQ号2"

oneBotMode: "forward-ws"
oneBotUrl: "ws://127.0.0.1:3001"
oneBotToken: "与NapCat一致的Token"

sqlitePath: "./MoBoxBot/data/bot.db"
pluginDir: "./MoBoxBot/plugins"
enableMessageLog: false
```

权限说明：

| 配置 | 含义 |
|---|---|
| `botOwner` | 机器人所有者，支持多个 QQ，必须用英文逗号分隔，例如 `"123456,234567"` |
| `botAdmin` | 机器人管理员，多个 QQ 必须用英文逗号分隔，例如 `"123456,234567"` |

修改配置后，可以在控制台执行：

```text
reload
```

监听端口、数据库连接等配置需要重启进程。

`enableMessageLog` 开启后，控制台会输出：

- 群聊和私聊消息
- 通知、请求和元事件，其中 heartbeat 不显示
- 机器人成功发送的群聊和私聊内容

图片统一显示为 `[图片]`。例如：

```text
[消息] 群 测试群(123456) 用户 测试用户(234567)：/ping
[发送] 群 123456：[图片]
```

## 5. NapCat 配置

正向 WebSocket 对应 NapCat 的 `network.websocketServers`：

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
        "token": "与MoBoxBot一致的Token",
        "enableForcePushEvent": true,
        "debug": false,
        "heartInterval": 30000
      }
    ]
  }
}
```

生产环境不要使用空 Token，也不要把 OneBot 端口暴露到公网。

## 6. 启动顺序

1. 启动 NTQQ 并登录。
2. 启动 NapCat。
3. 确认 NapCat 的 OneBot WS 端口 `3001` 已监听。
4. 运行 `run.bat` 启动 MoBoxBot。

正常情况下日志会显示：

```text
正向 WebSocket 已连接！
```

## 7. 控制台命令

| 命令 | 说明 |
|---|---|
| `status` | 查看版本、连接状态、命令数量和插件数量 |
| `plugin list` | 查看插件列表 |
| `plugin info <name>` | 查看插件详情 |
| `plugin enable <name>` | 启用插件 |
| `plugin disable <name>` | 停用插件 |
| `plugin reload <name>` | 重载插件，开发模式使用 |
| `reload` | 重载配置 |
| `debug` | 切换调试模式，开启后控制台显示 WS 收到的原始内容 |
| `exit` / `stop` | 退出 MoBoxBot |

## 8. 聊天命令

聊天命令默认前缀是 `/`，可以在 `config.yml` 修改。

| 命令 | 权限 | 来源 | 说明 |
|---|---|---|---|
| `/ping` | `BOT_ADMIN` | `MBB-Ping` | 测试机器人是否运行中 |
| `/ai status` | `OWNER` | `MBB-AI` | 图片查看 AI 服务状态 |
| `/ai usage [天数]` | `OWNER` | `MBB-AI` | 图片查看 AI 请求与 Token 统计 |
| `/ai reload` | `OWNER` | `MBB-AI` | 重载 AI 配置 |
| `/chat <内容>` | `BOT_ADMIN` 或白名单 | `MBB-Chat` | 与 AI 对话，每人独立上下文 |
| `/chat new` | `BOT_ADMIN` 或白名单 | `MBB-Chat` | 清空自己的 AI 上下文 |
| `/chat persona` | `BOT_ADMIN` 或白名单 | `MBB-Chat` | 查看自己的 AI 人设 |
| `/chat persona set/reset` | `BOT_ADMIN` 或白名单 | `MBB-Chat` | 设置或恢复自己的 AI 人设 |
| `/chat whitelist add/remove/list` | `BOT_ADMIN` | `MBB-Chat` | 管理 AI 对话白名单 |
| `/chatstat group [群号] [天数]` | `BOT_ADMIN` | `MBB-ChatStat` | 图片统计群聊内容 |
| `/chatstat user <QQ> [天数]` | `BOT_ADMIN` | `MBB-ChatStat` | 图片统计某人在所有可见群的发言 |
| `/plugins` / `/plugins list` | `OWNER` | `MBB-Plugins` | 显示当前插件列表图片 |
| `/plugins info <插件名>` | `OWNER` | `MBB-Plugins` | 查看插件详情 |
| `/plugins enable/disable/reload <插件名>` | `OWNER` | `MBB-Plugins` | 管理插件，不能停用或重载自身 |
| `/status` | `BOT_ADMIN` | `MBB-Status` | 显示 CPU、内存、硬盘、网络状态 |
| `/help` | `EVERYONE` | `MBB-Help` | 命令帮助图片 |
| `/version` | `BOT_ADMIN` | `MBB-Version` | 版本信息图片 |
| `/reload` | `OWNER` | `MBB-Reload` | 重载主程序配置 |
| `/remind <时间> <内容>` | `BOT_ADMIN` | `MBB-Remind` | 定时提醒，支持 `s/m/h/d` |
| `/random [min] [max]` | `EVERYONE` | `MBB-Random` | 指定范围随机数 |
| `/admin list` | `OWNER` | `MBB-Admin` | 查看管理员列表 |
| `/admin add <QQ>` | `OWNER` | `MBB-Admin` | 添加管理员 |
| `/admin remove <QQ>` | `OWNER` | `MBB-Admin` | 移除管理员 |
| `/welcome` | `BOT_ADMIN` | `MBB-Welcome` | 切换当前群进群退群消息 |
| `/poll <时长> <问题> [选项...]` | `BOT_ADMIN` | `MBB-Poll` | 发起限时投票 |
| `/vote <序号>` | `EVERYONE` | `MBB-Poll` | 参与当前群投票 |
| `来只猪猪` | `EVERYONE` | `MBB-PigHub` | 关键词触发随机猪猪图片 |

内置闭麦指令：

| 指令 | 权限 | 说明 |
|---|---|---|
| `/quiet` | `BOT_ADMIN` | 切换闭麦状态，别名 `/muteself`、`/selfmute` |

闭麦开启后，机器人不响应任何命令、消息、通知和戳一戳；只接受下一次管理员闭麦开关命令用于解除闭麦。

命令支持 @机器人 使用，例如：

```text
@MoBoxBot /ping
@MoBoxBot ping
```

群里有多个 MoBoxBot 时，可以用这种方式指定要调用的机器人。

`/help` 会读取当前已注册的全部命令，按权限分区显示，并且只显示当前发送者有权限使用的命令。

权限不足时不会执行命令。

## 9. 插件安装

把插件 JAR 放入：

```text
./MoBoxBot/plugins/
```

插件 JAR 根目录必须有 `plugin.json`，主类必须继承：

```text
org.moboxlab.moboxbot.API.Plugin
```

插件开发规范见：

- [PLUGIN.md](PLUGIN.md)
- [API.md](API.md)
- [ONEBOT.md](ONEBOT.md)

当前常用插件工程位于：

```text
D:\CodeX\Projects\MBB-Plugins
```

包含：

- `MBB-Ping`：`/ping`
- `MBB-Plugins`：`/plugins`
- `MBB-Status`：`/status`

官方插件开发示例位于：

```text
https://github.com/MossCG/MBB-ExamplePlugin
```

## 10. 常见问题

### 控制台中文乱码

使用 `run.bat` 启动。脚本已经设置：

```bat
chcp 65001
java -Dfile.encoding=UTF-8 -jar MoBoxBot.jar
```

直接手动执行 `java -jar` 时，请先执行：

```bat
chcp 65001
```

### 连接 3001 失败

检查：

1. NapCat 是否启动。
2. NapCat 的 `websocketServers` 是否启用。
3. 端口是否为 `3001`。
4. Token 是否与 `config.yml` 一致。

### 插件显示 FAILED

查看日志中的失败原因，常见原因：

- 缺少 `plugin.json`
- 主类没有继承 `API.Plugin`
- `apiVersion` 主版本不一致
- 缺少强依赖
- 存在循环依赖

### 命令没有回复

检查：

1. 发送账号是否在 `botOwner` 或 `botAdmin` 中。
2. 消息是否以 `/` 开头。
3. 机器人是否有权限在群里发言。
4. 是否触发命令冷却。

## 11. 升级与备份

升级前备份：

```text
./MoBoxBot/config.yml
./MoBoxBot/data/bot.db
./MoBoxBot/plugins/
```

替换 `MoBoxBot.jar` 后重启。`config.yml` 和 `struct-sqlite.sql` 只在文件不存在时释放，升级时不会覆盖已有配置。

## 12. 安全注意

- 不要在仓库里提交 Token、Cookie、密码和真实数据库。
- 不要使用空 OneBot Token。
- 不要把 OneBot WS 端口直接暴露到公网。
- 插件是可执行代码，只加载可信插件。
- 生产环境更新插件建议重启进程。
