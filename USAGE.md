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

示例插件：

```powershell
cd example-plugin
.\build.ps1
```

产物：

```text
example-plugin\out\MoBoxBot-ExamplePlugin.jar
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
botOwner: "你的QQ号"
botAdmin: "管理员QQ号，多个用逗号分隔"

oneBotMode: "forward-ws"
oneBotUrl: "ws://127.0.0.1:3001"
oneBotToken: "与NapCat一致的Token"

sqlitePath: "./MoBoxBot/data/bot.db"
pluginDir: "./MoBoxBot/plugins"
```

权限说明：

| 配置 | 含义 |
|---|---|
| `botOwner` | 机器人所有者，拥有最高权限 |
| `botAdmin` | 机器人管理员，多个 QQ 用英文逗号分隔 |

修改配置后，可以在控制台执行：

```text
reload
```

监听端口、数据库连接等配置需要重启进程。

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
| `debug` | 切换调试模式 |
| `exit` / `stop` | 退出 MoBoxBot |

## 8. 聊天命令

聊天命令默认前缀是 `/`，可以在 `config.yml` 修改。

| 命令 | 权限 | 说明 |
|---|---|---|
| `/plugins` | `BOT_ADMIN` | 显示当前插件列表 |
| `/ping` | `BOT_ADMIN` | 示例插件测试命令，返回 `pong` |
| `/hello` | `BOT_ADMIN` | 示例管理员命令，返回欢迎语 |

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
