# MoBoxBot 部署文档

## 1. 部署组成

MoBoxBot 需要两个进程：

```text
NTQQ -> NapCatQQ -> OneBot 11 -> MoBoxBot
```

- NapCatQQ 负责登录 QQ、适配 NTQQ 协议和 QQ 风控。
- MoBoxBot 负责 OneBot 11 通信、插件、命令和业务。

## 2. Windows 部署

### 安装 Java 8

确认命令可用：

```text
java -version
javac -version
```

### 部署目录

```text
MoBoxBot-Deploy/
├─ MoBoxBot.jar
├─ run.bat
└─ MoBoxBot/
```

启动：

```text
run.bat
```

首次启动会释放：

```text
./MoBoxBot/config.yml
./MoBoxBot/data/
./MoBoxBot/logs/
./MoBoxBot/plugins/
```

### 配置

修改 `./MoBoxBot/config.yml`：

```yaml
enable: true
botOwner: "你的QQ号"
botAdmin: "管理员QQ号1,管理员QQ号2"
oneBotMode: "forward-ws"
oneBotUrl: "ws://127.0.0.1:3001"
oneBotToken: "与NapCat一致的Token"
```

### 配置 NapCat

正向 WebSocket：

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

启动 NapCat 并登录 QQ，再启动 MoBoxBot。

## 3. Linux 部署

### 安装 Java 8

Debian / Ubuntu：

```bash
sudo apt update
sudo apt install openjdk-8-jre-headless
```

确认：

```bash
java -version
```

### 运行目录

```text
/opt/moboxbot/
├─ MoBoxBot.jar
├─ start.sh
└─ MoBoxBot/
```

启动脚本：

```bash
#!/usr/bin/env bash
cd /opt/moboxbot
exec java -Dfile.encoding=UTF-8 -Djava.awt.headless=true -jar MoBoxBot.jar
```

赋予执行权限：

```bash
chmod +x start.sh
```

### systemd

`/etc/systemd/system/moboxbot.service`：

```ini
[Unit]
Description=MoBoxBot
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/moboxbot
ExecStart=/usr/bin/java -Dfile.encoding=UTF-8 -Djava.awt.headless=true -jar /opt/moboxbot/MoBoxBot.jar
Restart=on-failure
RestartSec=5
User=moboxbot

[Install]
WantedBy=multi-user.target
```

启用：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now moboxbot
sudo systemctl status moboxbot
```

## 4. 反向 WebSocket

如果 NapCat 和 MoBoxBot 不在同一台机器，可以使用反向 WebSocket：

```yaml
oneBotMode: "reverse-ws"
oneBotHost: "0.0.0.0"
oneBotPort: 3001
oneBotPath: "/onebot"
oneBotToken: "与NapCat一致的Token"
```

NapCat 配置 `network.websocketClients`：

```json5
{
  "network": {
    "websocketClients": [
      {
        "name": "MoBoxBot",
        "enable": true,
        "url": "ws://MoBoxBot地址:3001/onebot",
        "messagePostFormat": "array",
        "reportSelfMessage": false,
        "reconnectInterval": 5000,
        "token": "与MoBoxBot一致的Token",
        "heartInterval": 30000
      }
    ]
  }
}
```

反向模式下不要在公网裸奔，必须使用防火墙和 Token。

## 5. 插件部署

把插件 JAR 放到：

```text
./MoBoxBot/plugins/
```

新插件首次加载需要重启 MoBoxBot。

已加载插件可以在控制台执行：

```text
plugin reload <插件名>
plugin disable <插件名>
plugin enable <插件名>
```

## 6. 升级与备份

升级前备份：

```text
./MoBoxBot/config.yml
./MoBoxBot/data/bot.db
./MoBoxBot/plugins/
```

替换 `MoBoxBot.jar` 后重启。

`config.yml` 和 `struct-sqlite.sql` 只在文件不存在时释放，升级不会覆盖已有配置。

## 7. 常见问题

### 连接 3001 失败

1. NapCat 是否启动。
2. `websocketServers` 或 `websocketClients` 是否启用。
3. Token 是否一致。
4. 防火墙是否放行。

### 控制台乱码

使用 `run.bat` 启动，或手动执行：

```text
chcp 65001
java -Dfile.encoding=UTF-8 -Djava.awt.headless=true -jar MoBoxBot.jar
```

### 插件 FAILED

查看日志中的失败原因。常见原因包括：

- 缺少 `plugin.json`
- 主类没有继承 `API.Plugin`
- `apiVersion` 主版本不一致
- 缺少强依赖
- 循环依赖

### debug

控制台执行：

```text
debug
```

开启后会显示 OneBot WS 收到的全部原始内容，再执行一次关闭。
