# 安全策略

## 支持范围

当前只维护主分支上的最新开发版本。

## 报告安全问题

请不要在公开 Issue 中贴出可利用细节、真实 Token、Cookie、密码或个人账号信息。

先通过私密渠道联系维护者，并提供：

- 影响版本
- 复现步骤
- 影响范围
- 建议修复方式

## 凭据策略

- 仓库中不允许提交真实 `oneBotToken`。
- 仓库中不允许提交 QQ 密码、Cookie、API Key、AccessKeySecret。
- `config.yml` 模板中的敏感字段必须为空。
- 插件配置中的第三方 API Key 由部署方自行填写，不能硬编码。
- 日志和文档不能输出完整 Token。

## 部署安全

- 不要使用空 OneBot Token。
- 不要把 OneBot 端口暴露到公网。
- 反向 WebSocket 必须配合防火墙和 Token。
- 插件是可执行代码，只加载可信 JAR。
- 发布前建议执行一次：

```powershell
git grep -n -I -E "oneBotToken\s*[:=]\s*[`"'][^`"']{8,}|password\s*[:=]\s*[`"'][^`"']{6,}|apiKey\s*[:=]\s*[`"'][^`"']{8,}|AccessKeySecret|LTAI[0-9A-Za-z]{12,}"
```

命中字段名是正常的，命中真实值要先脱敏并轮换凭据。
