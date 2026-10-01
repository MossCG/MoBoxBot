# 参与开发

## 仓库拆分

项目由两个独立 Git 仓库组成：

| 仓库 | 路径 | 说明 |
|---|---|---|
| 主程序 | `MoBoxBot` | NapCat OneBot 客户端、插件内核、API |
| 插件 | `MBB-Plugins` | MBB-* 独立插件 |

插件仓库的改动不需要主程序更新版本号，除非同时修改了主程序 API 或核心代码。

## 环境

- Java 8
- PowerShell 5.1 或 PowerShell 7
- Git

## 构建主程序

```powershell
.\build.ps1
```

产物：

```text
out\MoBoxBot.jar
```

## 构建插件

```powershell
cd D:\CodeX\Projects\MBB-Plugins
.\build-all.ps1 -Bot D:\CodeX\Projects\MoBoxBot\out\MoBoxBot.jar
```

## 版本与提交

主程序版本格式：

```text
V大版本.小版本.小更新.小修正.四位时间戳
```

示例：

```text
V0.4.0.0.0048 插件管理、Welcome、Poll、PigHub
```

规则：

1. 主程序代码或 API 变化，按主程序版本号提交。
2. 纯文档调整使用 `docs: 中文摘要`。
3. 插件仓库独立提交，使用插件仓库自己的版本节奏。
4. 插件改动不影响主程序时，不改主程序 `BasicInfo.version`。

## 代码风格

- Java 8
- 中文注释、中文日志
- 不使用 Spring、Lombok、PF4J、MyBatis
- 业务工具类尽量保持静态
- 异常不能拖垮主程序
- 参数、配置、日志和数据库操作遵循 [STYLE.md](STYLE.md)

## 插件开发

插件只允许依赖 `org.moboxlab.moboxbot.API`，具体规范见 [PLUGIN.md](PLUGIN.md)。

## 提交前检查

1. `build.ps1` 编译通过。
2. 插件 `build-all.ps1` 编译通过。
3. 不提交 `out/`、运行目录、数据库、日志和真实 Token。
4. 文档与代码同步。
5. 新增命令标明权限。
