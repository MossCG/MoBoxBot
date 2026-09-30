# MoBoxBot

MoBoxBot 是一个基于 Java 8 的 QQ 机器人客户端，协议端使用 NapCatQQ，通信协议使用 OneBot 11，功能通过 Spigot 式插件加载。

## 功能

- 正向 WebSocket 默认连接，反向 WebSocket 可选
- OneBot 11 事件接收、消息发送和 Action 调用
- 插件扫描、依赖排序、独立类加载器和生命周期
- 事件总线与 `@EventHandler`
- 聊天命令、权限和冷却
- 控制台命令：`status`、`plugin`、`reload`、`debug`、`exit`
- SQLite 插件记录、群配置、权限、命令日志和插件数据

## 环境

- Java 8
- NapCatQQ
- Windows 构建脚本 `build.ps1`

## 构建

```powershell
.\build.ps1
```

产物：

```text
out\MoBoxBot.jar
```

## 运行

```text
java -jar out\MoBoxBot.jar
```

首次运行会释放：

```text
./MoBoxBot/
├─ config.yml
├─ data/
├─ logs/
├─ plugins/
└─ dependency/
```

在 `./MoBoxBot/config.yml` 中设置：

```yaml
enable: true
oneBotMode: "forward-ws"
oneBotUrl: "ws://127.0.0.1:3001"
oneBotToken: "你的 Token"
```

## NapCat 配置

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
        "token": "你的 Token"
      }
    ]
  }
}
```

生产环境不要使用空 Token，也不要把端口直接暴露到公网。

## 插件

插件 JAR 放到：

```text
./MoBoxBot/plugins/
```

插件开发规范见 [PLUGIN.md](PLUGIN.md)，API 草案见 [API.md](API.md)，OneBot 覆盖见 [ONEBOT.md](ONEBOT.md)。

示例插件：

```powershell
cd example-plugin
.\build.ps1
```

产物：

```text
example-plugin/out/MoBoxBot-ExamplePlugin.jar
```

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。

第三方依赖与声明见 [NOTICE](NOTICE)。
