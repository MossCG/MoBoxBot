package org.moboxlab.moboxbot.Database;

import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxlib.File.FileCheck;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 建表脚本初始化
 */
public class TableInitializer {
    public static void init() {
        String path = BasicInfo.runDir+"/struct-sqlite.sql";
        FileCheck.checkFileExist(path,"struct-sqlite.sql");
        try {
            String text = new String(Files.readAllBytes(Paths.get(path)),StandardCharsets.UTF_8);
            String[] statements = text.split(";");
            int count = 0;
            for (String statement : statements) {
                String sql = statement.trim();
                if (sql.isEmpty()) continue;
                if (sql.startsWith("--")) {
                    int index = sql.indexOf('\n');
                    if (index < 0) continue;
                    sql = sql.substring(index + 1).trim();
                }
                if (sql.isEmpty() || sql.startsWith("--")) continue;
                if (SqlExecutor.execute(sql)) count++;
            }
            BasicInfo.logger.sendInfo("数据库结构初始化完成：执行 "+count+" 条语句！");
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
    }
}
