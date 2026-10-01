# MoBoxBot

MoBoxBot 是一个基于 Java 8 的 QQ 机器人客户端。

- QQ 协议端：NapCatQQ
- 通信协议：OneBot 11
- 插件内核：Spigot 风格自研
- 默认连接：正向 WebSocket
- 数据存储：SQLite

MoBoxBot 只负责机器人平台、连接、事件、命令和插件加载；具体功能通过独立插件实现。

## 功能

- OneBot 11 正向 WebSocket、反向 WebSocket
- Token 鉴权、心跳检测、断线自动重连
- 群消息、私聊消息、通知、请求、元事件
- 文本、图片、@、回复消息
- 插件扫描、依赖排序、独立类加载器、生命周期
- `@EventHandler` 事件总线
- 聊天命令、权限、冷却
- 内置闭麦指令
- 图片渲染 API
- SQLite 插件记录、群配置、权限和插件数据
- 控制台调试与 WS 原始消息日志

## 环境

- Java 8
- Windows 或 Linux
- NapCatQQ
- NTQQ 客户端

## 快速开始

### 1. 构建

```powershell
.\build.ps1
```

产物：

```text
out\MoBoxBot.jar
```

### 2. 配置

首次运行会释放：

```text
./MoBoxBot/config.yml
```

至少修改：

```yaml
enable: true
botOwner: "你的QQ号"
botAdmin: ""
oneBotMode: "forward-ws"
oneBotUrl: "ws://127.0.0.1:3001"
oneBotToken: "与NapCat一致的Token"
```

`botOwner` 和 `botAdmin` 都支持多个 QQ，使用英文逗号分隔。

### 3. 配置 NapCat

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

### 4. 启动

```text
run.bat
```

Windows 控制台会使用 UTF-8 并且自动切换工作目录。

## 目录结构

```text
MoBoxBot/
├─ src/main/java/org/moboxlab/moboxbot/
├─ src/main/resources/
├─ depend/
├─ example-plugin/
├─ build.ps1
├─ run.bat
└─ .github/workflows/
```

运行目录：

```text
./MoBoxBot/
├─ config.yml
├─ data/
├─ logs/
├─ plugins/
└─ dependency/
```

## 常用命令

| 命令 | 权限 | 说明 |
|---|---|---|
| `/ping` | `BOT_ADMIN` | 测试是否运行中 |
| `/plugins` | `OWNER` | 插件列表与管理 |
| `/status` | `BOT_ADMIN` | 系统状态 |
| `/help` | `EVERYONE` | 当前权限可用命令 |
| `/version` | `BOT_ADMIN` | 版本信息 |
| `/reload` | `OWNER` | 重载主程序配置 |
| `/remind <时间> <内容>` | `BOT_ADMIN` | 定时提醒 |
| `/random [min] [max]` | `EVERYONE` | 随机数 |
| `/admin list/add/remove` | `OWNER` | 管理员管理 |
| `/welcome` | `BOT_ADMIN` | 群欢迎开关 |
| `/poll` / `/vote` | `BOT_ADMIN` / `EVERYONE` | 群投票 |

命令支持：

```text
@MoBoxBot /ping
@MoBoxBot ping
```

## 文档

- [USAGE.md](USAGE.md)：使用、配置、命令和常见问题
- [DEPLOY.md](DEPLOY.md)：Windows、Linux 和 NapCat 部署
- [PLUGIN.md](PLUGIN.md)：插件开发规范
- [API.md](API.md)：插件 API 草案
- [ONEBOT.md](ONEBOT.md)：OneBot 11 覆盖清单
- [STYLE.md](STYLE.md)：代码风格
- [CONTRIBUTING.md](CONTRIBUTING.md)：参与开发
- [SECURITY.md](SECURITY.md)：安全与凭据策略
- [update.md](update.md)：版本更新日志

## 相关仓库

- 插件仓库：`D:\CodeX\Projects\MBB-Plugins`
- 基础库：`D:\CodeX\Projects\MoBoxLib`

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。

第三方依赖与声明见 [NOTICE](NOTICE)。
