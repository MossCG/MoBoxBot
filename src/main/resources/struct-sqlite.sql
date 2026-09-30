--MoBoxBot SQLite 结构
--结构版本用 PRAGMA user_version 管理

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS `bot_plugin_record` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `name` TEXT NOT NULL,
    `version` TEXT NOT NULL DEFAULT '',
    `apiVersion` TEXT NOT NULL DEFAULT '',
    `state` TEXT NOT NULL DEFAULT 'LOADED',
    `loadTime` INTEGER NOT NULL DEFAULT 0,
    `message` TEXT NOT NULL DEFAULT '',
    `fileName` TEXT NOT NULL DEFAULT '',
    `filePath` TEXT NOT NULL DEFAULT '',
    `dataFolder` TEXT NOT NULL DEFAULT '',
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_plugin_record_name` ON `bot_plugin_record` (`name`);

CREATE TABLE IF NOT EXISTS `bot_group_config` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `groupID` TEXT NOT NULL,
    `enable` INTEGER NOT NULL DEFAULT 1,
    `commandPrefix` TEXT NOT NULL DEFAULT '',
    `welcomeEnable` INTEGER NOT NULL DEFAULT 0,
    `leaveEnable` INTEGER NOT NULL DEFAULT 0,
    `antiRecallEnable` INTEGER NOT NULL DEFAULT 0,
    `extra` TEXT NOT NULL DEFAULT '{}',
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_group_config_group` ON `bot_group_config` (`groupID`);

CREATE TABLE IF NOT EXISTS `bot_group_user` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `groupID` TEXT NOT NULL,
    `userID` TEXT NOT NULL,
    `permission` TEXT NOT NULL DEFAULT 'EVERYONE',
    `enable` INTEGER NOT NULL DEFAULT 1,
    `lastActive` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_group_user_unique` ON `bot_group_user` (`groupID`,`userID`);

CREATE TABLE IF NOT EXISTS `bot_user` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `userID` TEXT NOT NULL,
    `nickname` TEXT NOT NULL DEFAULT '',
    `permission` TEXT NOT NULL DEFAULT 'EVERYONE',
    `enable` INTEGER NOT NULL DEFAULT 1,
    `lastActive` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_user_user` ON `bot_user` (`userID`);

CREATE TABLE IF NOT EXISTS `bot_plugin_data` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `pluginName` TEXT NOT NULL,
    `dataKey` TEXT NOT NULL,
    `dataValue` TEXT NOT NULL DEFAULT '',
    `expireTime` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0,
    `updateTime` INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_bot_plugin_data_unique` ON `bot_plugin_data` (`pluginName`,`dataKey`);
CREATE INDEX IF NOT EXISTS `idx_bot_plugin_data_expire` ON `bot_plugin_data` (`expireTime`);

CREATE TABLE IF NOT EXISTS `bot_command_log` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `messageID` TEXT NOT NULL DEFAULT '',
    `groupID` TEXT NOT NULL DEFAULT '',
    `userID` TEXT NOT NULL DEFAULT '',
    `command` TEXT NOT NULL DEFAULT '',
    `args` TEXT NOT NULL DEFAULT '',
    `success` INTEGER NOT NULL DEFAULT 0,
    `costTime` INTEGER NOT NULL DEFAULT 0,
    `createTime` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_bot_command_log_time` ON `bot_command_log` (`createTime`);
CREATE INDEX IF NOT EXISTS `idx_bot_command_log_group_time` ON `bot_command_log` (`groupID`,`createTime`);

CREATE TABLE IF NOT EXISTS `bot_message_log` (
    `ID` INTEGER PRIMARY KEY AUTOINCREMENT,
    `messageID` TEXT NOT NULL DEFAULT '',
    `messageType` TEXT NOT NULL DEFAULT '',
    `groupID` TEXT NOT NULL DEFAULT '',
    `userID` TEXT NOT NULL DEFAULT '',
    `rawMessage` TEXT NOT NULL DEFAULT '',
    `createTime` INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS `idx_bot_message_log_time` ON `bot_message_log` (`createTime`);

PRAGMA user_version = 1;
