# MoBoxBot

MoBoxBot 是一个基于 Java 8 的 QQ 机器人客户端。

- QQ 协议端：NapCatQQ
- 通信协议：OneBot 11
- 插件内核：Spigot 风格自研
- 默认连接：正向 WebSocket
- 数据存储：SQLite

MoBoxBot 只负责机器人平台、连接、事件、命令和插件加载；具体功能通过独立插件实现。

**重要：MoBoxBot 是高度模块化的项目。**

主程序只内置必要的连接、事件、权限、闭麦和控制台能力。`/ping`、`/plugins`、`/status`、`/welcome`、`/poll`、`/random`、`/remind`、`/poke` 等功能都由独立插件提供。

插件仓库：

```text
https://github.com/MossCG/MBB-Plugins
```

如果需要完整插件列表、插件指令和插件文档，请移步 MBB-Plugins。

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
- `enableMessageLog` 消息日志：群消息、推送事件和机器人发送内容，图片显示为 `[图片]`

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

## 内置指令

主程序内置闭麦指令：

| 指令 | 权限 | 说明 |
|---|---|---|
| `/quiet` | `BOT_ADMIN` | 切换闭麦状态 |
| `/muteself` | `BOT_ADMIN` | `/quiet` 别名 |
| `/selfmute` | `BOT_ADMIN` | `/quiet` 别名 |

闭麦开启后，机器人不响应任何命令、消息、通知和戳一戳，只接受下一次管理员解除命令。

命令支持 @机器人 使用：

```text
@MoBoxBot /quiet
@MoBoxBot quiet
```

其他聊天指令由插件提供，请查看：

```text
https://github.com/MossCG/MBB-Plugins
```

主程序只提供以下控制台命令：

```text
status
plugin list
plugin info <name>
plugin enable <name>
plugin disable <name>
plugin reload <name>
reload
debug
exit / stop
```

## 发布

正式发布前同步更新：

```text
version.txt
src/main/java/org/moboxlab/moboxbot/BasicInfo.java
update.md
```

推送 `master` 后，GitHub Actions 会自动构建并发布 Release。

## 文档

- [USAGE.md](USAGE.md)：使用、配置、命令和常见问题
- [DEPLOY.md](DEPLOY.md)：Windows、Linux 和 NapCat 部署
- [PLUGIN.md](PLUGIN.md)：插件开发规范
- [API.md](API.md)：插件 API 0.1 冻结文档
- [ONEBOT.md](ONEBOT.md)：OneBot 11 覆盖清单
- [STYLE.md](STYLE.md)：代码风格
- [CONTRIBUTING.md](CONTRIBUTING.md)：参与开发
- [SECURITY.md](SECURITY.md)：安全与凭据策略
- [update.md](update.md)：版本更新日志

## 开发协作

| 仓库 | 用途 |
|---|---|
| [MBB-ExamplePlugin](https://github.com/MossCG/MBB-ExamplePlugin) | 官方插件开发示例，覆盖 API 0.1 的主要调用方式 |
| [MBB-Plugins](https://github.com/MossCG/MBB-Plugins) | MoBoxBot 常用插件集合 |
| [MoBoxLib](https://github.com/MossCG/MoBoxLib) | MoBoxBot 与 MBB-* 插件共用的 Java 8 基础库 |

开发新插件时，优先从 `MBB-ExamplePlugin` 复制结构。

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。

第三方依赖与声明见 [NOTICE](NOTICE)。
