# ONEBOT.md — MoBoxBot OneBot 11 覆盖清单

> 状态：M0 草案
> 协议端：NapCatQQ
> 默认连接：正向 WebSocket
> 参考：`_ref/NapCatDocs/src/onebot/`、`_ref/NapCatQQ/packages/napcat-onebot/`

## 1. 文档来源

| 内容 | 路径 | 说明 |
|---|---|---|
| 网络基础 | `_ref/NapCatDocs/src/onebot/network.md` | HTTP / WebSocket 角色与示例 |
| 事件基础 | `_ref/NapCatDocs/src/onebot/basic_event.md` | OneBot 事件基础结构 |
| 事件详情 | `_ref/NapCatDocs/src/onebot/event.md` | 事件继承关系与字段 |
| 消息段 | `_ref/NapCatDocs/src/onebot/segment.md` | 消息段类型与字段 |
| API 列表 | `_ref/NapCatDocs/src/onebot/api.md` | NapCat 实现的 OneBot API |
| NapCat 差异 | `_ref/NapCatDocs/src/onebot/napcat.md` | fileurl / base64 等差异 |
| 网络配置 | `_ref/NapCatDocs/src/config/basic.md` | `websocketServers` / `websocketClients` |
| 源码配置结构 | `_ref/NapCatQQ/packages/napcat-onebot/config/config.ts` | TypeBox Schema 与默认值 |

文档 API 快照当前最高版本为 `4.18.28`，实际实现时以实际 NapCat 版本 + 最新快照交叉确认。

## 2. 连接模式

### 2.1 正向 WebSocket（默认）

NapCat 作为 WebSocket 服务端，MoBoxBot 作为客户端主动连接。

NapCat 配置来自 `network.websocketServers`：

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
        "token": "",
        "enableForcePushEvent": true,
        "debug": false,
        "heartInterval": 30000
      }
    ]
  }
}
```

Java 侧连接：

```text
ws://127.0.0.1:3001
```

字段说明：

| 字段 | 说明 | MoBoxBot 默认 |
|---|---|---|
| `name` | 唯一标识 | `MoBoxBot` |
| `enable` | 是否启用 | `true` |
| `host` | 监听主机 | `127.0.0.1` |
| `port` | 监听端口 | `3001` |
| `messagePostFormat` | 消息上报格式 | `array` |
| `reportSelfMessage` | 是否上报自身消息 | `false` |
| `token` | 鉴权密钥 | 生产环境必须非空 |
| `enableForcePushEvent` | 强制推送事件 | `true` |
| `debug` | raw 数据上报 | `false` |
| `heartInterval` | 心跳周期 | `30000` |

### 2.2 反向 WebSocket（备选）

NapCat 作为 WebSocket 客户端，主动连接 MoBoxBot 的 WebSocket 服务端。

NapCat 配置来自 `network.websocketClients`：

```json5
{
  "network": {
    "websocketClients": [
      {
        "name": "MoBoxBot",
        "enable": true,
        "url": "ws://127.0.0.1:8082",
        "messagePostFormat": "array",
        "reportSelfMessage": false,
        "reconnectInterval": 5000,
        "token": "",
        "debug": false,
        "heartInterval": 30000
      }
    ]
  }
}
```

正向与反向共用同一套事件、Action 和消息段模型，只替换传输层。

### 2.3 认证与安全

- 支持 `Authorization: Bearer <token>`。
- Token 从 `config.yml` 读取，不硬编码。
- 日志中不输出 Token。
- 生产环境不得使用空 Token，不得把 WS 端口直接暴露到公网。
- 参考安全事件：`_ref/NapCatDocs/src/develop/security.md`。

## 3. 事件覆盖

### 3.1 事件分类

| OneBot 事件 | MoBoxBot 事件类 | 优先级 | 备注 |
|---|---|---|---|
| `message.private` | `PrivateMessageEvent` | P0 | 私聊消息 |
| `message.group` | `GroupMessageEvent` | P0 | 群消息 |
| `notice.group_upload` | `NoticeEvent` | P1 | 群文件上传 |
| `notice.group_admin` | `NoticeEvent` | P1 | 群管理员变动 |
| `notice.group_decrease` | `NoticeEvent` | P1 | 群成员减少 |
| `notice.group_increase` | `NoticeEvent` | P1 | 群成员增加 |
| `notice.group_ban` | `NoticeEvent` | P1 | 群禁言 |
| `notice.friend_add` | `NoticeEvent` | P1 | 好友添加 |
| `notice.group_recall` | `NoticeEvent` | P1 | 群消息撤回 |
| `notice.friend_recall` | `NoticeEvent` | P1 | 好友消息撤回 |
| `notice.poke` | `NoticeEvent` | P1 | 戳一戳 |
| `request.friend` | `RequestEvent` | P0 | 加好友请求 |
| `request.group` | `RequestEvent` | P0 | 加群请求 |
| `meta_event.lifecycle` | `MetaEvent` | P0 | 生命周期 |
| `meta_event.heartbeat` | `MetaEvent` | P0 | 心跳 |
| 未识别事件 | `RawEvent` | 兜底 | 保留原始 JSON |

### 3.2 群消息事件关键字段

```json
{
  "post_type": "message",
  "message_type": "group",
  "sub_type": "normal",
  "message_id": 123456,
  "group_id": 10001,
  "user_id": 20002,
  "raw_message": "/ping",
  "message": [
    { "type": "text", "data": { "text": "/ping" } }
  ],
  "sender": {
    "user_id": 20002,
    "nickname": "示例用户",
    "role": "member"
  }
}
```

`GroupMessageEvent` 至少暴露：

- `groupID`
- `userID`
- `messageID`
- `rawMessage`
- `message`
- `sender`
- `raw`

### 3.3 心跳事件关键字段

```json
{
  "post_type": "meta_event",
  "meta_event_type": "heartbeat",
  "status": { "online": true, "good": true },
  "interval": 30000,
  "time": 1780000000
}
```

MoBoxBot 用它判断连接健康状态，并记录最近心跳时间。

## 4. Action 覆盖

### 4.1 P0

| Action | 用途 |
|---|---|
| `get_login_info` | 获取登录信息 |
| `get_status` | 获取在线状态 |
| `get_version_info` | 获取版本信息 |
| `send_private_msg` | 发私聊消息 |
| `send_group_msg` | 发群消息 |
| `delete_msg` | 撤回消息 |
| `get_msg` | 获取消息 |
| `get_friend_list` | 好友列表 |
| `get_group_list` | 群列表 |
| `get_group_info` | 群信息 |
| `get_group_member_info` | 群成员信息 |
| `get_group_member_list` | 群成员列表 |
| `set_friend_add_request` | 处理好友请求 |
| `set_group_add_request` | 处理加群请求 |
| `set_group_ban` | 群禁言 |
| `set_group_kick` | 群踢人 |
| `set_group_whole_ban` | 全员禁言 |
| `set_group_admin` | 设置管理员 |
| `set_group_card` | 设置群名片 |
| `set_group_name` | 设置群名 |
| `set_group_leave` | 退群 |
| `set_group_special_title` | 设置专属头衔 |
| `get_image` | 获取图片 |
| `get_record` | 获取语音 |
| `can_send_image` | 能否发图 |
| `can_send_record` | 能否发语音 |
| `clean_cache` | 清理缓存 |

### 4.2 P1

| Action | 用途 |
|---|---|
| `get_group_msg_history` | 群历史消息 |
| `get_friend_msg_history` | 私聊历史消息 |
| `send_forward_msg` | 发合并转发 |
| `get_forward_msg` | 取合并转发 |
| `set_msg_emoji_like` | 消息表情回应 |
| `mark_private_msg_as_read` | 私聊已读 |
| `mark_group_msg_as_read` | 群消息已读 |
| `set_group_portrait` | 设置群头像 |
| `set_group_sign` | 群打卡 |
| `get_group_file_url` | 群文件链接 |
| `get_private_file_url` | 私聊文件链接 |
| `download_file` | 下载文件 |

### 4.3 NapCat 扩展

NapCat 扩展 API 不在首批标准接口内，按需通过：

```java
OneBotClient.callAction("action_name",params);
```

扩展接口必须保留原始返回，插件自行判断 `status` / `retcode` / `data`。

## 5. 消息段覆盖

| 消息段 | 类型 | 优先级 | 备注 |
|---|---|---|---|
| 文本 | `text` | P0 | 最常用 |
| 表情 | `face` | P1 | QQ 表情 ID |
| 图片 | `image` | P0 | `file` / `url` |
| 语音 | `record` | P1 | `file` / `url` |
| 视频 | `video` | P1 | 可选 |
| @ | `at` | P0 | `qq` |
| 猜拳 | `rps` | P2 | 可选 |
| 骰子 | `dice` | P2 | 可选 |
| 窗口抖动 | `shake` | P2 | 可选 |
| 戳一戳 | `poke` | P1 | 依赖协议端 |
| 匿名 | `anonymous` | P2 | 视 NapCat 支持 |
| 链接分享 | `share` | P1 | — |
| 推荐好友 / 群 | `contact` | P2 | — |
| 位置 | `location` | P2 | — |
| 音乐 | `music` | P1 | — |
| 回复 | `reply` | P0 | 引用消息 |
| 合并转发 | `forward` | P1 | — |
| 合并转发节点 | `node` | P1 | — |
| XML | `xml` | P2 | — |
| JSON | `json` | P2 | — |
| 商城表情 | `mface` | P2 | — |
| 文件 | `file` | P1 | — |

消息段统一由 `MessageUtil` 构造，插件不直接拼复杂 JSON。

## 6. 请求与响应格式

### 6.1 发送群消息

请求：

```json
{
  "action": "send_group_msg",
  "params": {
    "group_id": 10001,
    "message": [
      { "type": "text", "data": { "text": "大家好！" } }
    ]
  },
  "echo": "uuid"
}
```

响应：

```json
{
  "status": "ok",
  "retcode": 0,
  "data": {
    "message_id": 123456
  },
  "echo": "uuid"
}
```

### 6.2 事件推送

```json
{
  "post_type": "message",
  "message_type": "group",
  "group_id": 10001,
  "user_id": 20002,
  "message": "你好"
}
```

## 7. 实现映射

| MoBoxBot 类 | 职责 |
|---|---|
| `OneBotMain` | 连接、认证、心跳、重连、状态 |
| `OneBotClient` | Action 调用、echo 关联、超时 |
| `OneBotEvent` | 原始事件解析与事件类映射 |
| `OneBotMessage` | 消息段解析 |
| `MessageUtil` | 消息段构造 |
| `OneBotEcho` | 等待中的响应表 |

## 8. 注意事项

- OneBot 11 已停滞，NapCat 会在扩展场景做差异化实现。
- `messagePostFormat` 固定用 `array`，比 `string` 更稳定。
- 图片链接可能过期，NapCat 文档提到约 2 小时有效期。
- 文件类接口可能需要 HTTP 端口，正向 WS 方案要确认 NapCat 的 HTTP 服务配置。
- `retcode` 非 0 时不能只看 `status`。
- `echo` 必须唯一，超时后要清理等待表。
- 发送类接口要限速，避免触发 QQ 风控。
- 未识别事件进入 `RawEvent`，不能丢弃。

## 9. 首版验收

1. 正向 WS 连接成功，Token 校验生效。
2. 心跳超时能检测并重连。
3. 能接收群消息、私聊消息、通知、请求和元事件。
4. 能发送文本、图片、@、回复消息。
5. `echo` 能正确关联响应，超时能清理。
6. 未识别事件能进 `RawEvent`。
7. 发送失败能拿到 `retcode` 和原始返回。
8. 日志不泄露 Token。

## 10. 参考

- `_ref/NapCatDocs/src/onebot/network.md`
- `_ref/NapCatDocs/src/onebot/event.md`
- `_ref/NapCatDocs/src/onebot/segment.md`
- `_ref/NapCatDocs/src/onebot/api.md`
- `_ref/NapCatDocs/src/onebot/napcat.md`
- `_ref/NapCatQQ/packages/napcat-onebot/config/config.ts`
