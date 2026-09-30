package org.moboxlab.moboxbot.Database;

import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxlib.File.FileCheck;
import org.moboxlab.moboxlib.Object.ObjectSQLiteInfo;
import org.moboxlab.moboxlib.SQLite.SQLiteManager;

import java.io.File;
import java.sql.Connection;

/**
 * 数据库入口
 * 业务代码统一走 getConnection()，不直接持有 SQLiteManager。
 */
public class DatabaseMain {
    public static SQLiteManager sqlite;

    private static final ThreadLocal<Connection> localConnection = new ThreadLocal<>();

    public static void init() {
        String path = BasicInfo.getConfigString("sqlitePath",BasicInfo.runDir+"/data/bot.db");
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null) FileCheck.checkDirExist(parent.getAbsolutePath());

        ObjectSQLiteInfo info = new ObjectSQLiteInfo();
        info.filePath = path;
        info.poolSize = BasicInfo.getConfigInt("sqlitePoolSize",2);

        sqlite = new SQLiteManager();
        sqlite.initSQLite(info,BasicInfo.logger,false);
        BasicInfo.logger.sendInfo("数据库已就绪：SQLite，文件："+path);
    }

    public static void checkDatabase() {
        if (sqlite == null) init();
    }

    public static Connection getConnection() {
        checkDatabase();
        Connection cached = localConnection.get();
        try {
            if (cached != null && !cached.isClosed()) return cached;
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
        Connection connection = sqlite.getConnection();
        localConnection.set(connection);
        return connection;
    }

    public static void close() {
        try {
            localConnection.remove();
            if (sqlite != null) sqlite.close();
            sqlite = null;
        } catch (Exception e) {
            BasicInfo.sendException(e);
        }
    }
}
