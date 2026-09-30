package org.moboxlab.moboxbot.Database;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.BasicInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 统一 SQL 执行封装
 * 所有方法串行执行，避免 SQLite 写锁冲突。
 */
public class SqlExecutor {
    private static final Object lock = new Object();

    public static List<JSONObject> query(String sql,Object... params) {
        List<JSONObject> result = new ArrayList<>();
        synchronized (lock) {
            try {
                PreparedStatement statement = prepare(sql,params);
                ResultSet set = statement.executeQuery();
                while (set.next()) result.add(readRow(set));
                set.close();
                statement.close();
            } catch (Exception e) {
                BasicInfo.sendException(e);
            }
        }
        return result;
    }

    public static JSONObject queryOne(String sql,Object... params) {
        List<JSONObject> list = query(sql,params);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static long count(String sql,Object... params) {
        JSONObject row = queryOne(sql,params);
        if (row == null || row.isEmpty()) return 0L;
        Object value = row.values().iterator().next();
        if (value == null) return 0L;
        return Long.parseLong(value.toString());
    }

    public static int update(String sql,Object... params) {
        synchronized (lock) {
            try {
                Connection connection = DatabaseMain.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                setParams(statement,params);
                int rows = statement.executeUpdate();
                statement.close();
                return rows;
            } catch (Exception e) {
                BasicInfo.sendException(e);
                return -1;
            }
        }
    }

    public static long insert(String sql,Object... params) {
        synchronized (lock) {
            try {
                Connection connection = DatabaseMain.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                setParams(statement,params);
                if (statement.executeUpdate() <= 0) {
                    statement.close();
                    return 0L;
                }
                ResultSet keys = statement.getGeneratedKeys();
                long id = keys.next() ? keys.getLong(1) : 0L;
                keys.close();
                statement.close();
                return id;
            } catch (Exception e) {
                BasicInfo.sendException(e);
                return 0L;
            }
        }
    }

    public static boolean execute(String sql) {
        synchronized (lock) {
            try {
                Connection connection = DatabaseMain.getConnection();
                Statement statement = connection.createStatement();
                statement.execute(sql);
                statement.close();
                return true;
            } catch (Exception e) {
                BasicInfo.sendException(e);
                return false;
            }
        }
    }

    public static boolean executeQuiet(String sql) {
        synchronized (lock) {
            try {
                Connection connection = DatabaseMain.getConnection();
                Statement statement = connection.createStatement();
                statement.execute(sql);
                statement.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static boolean transaction(SqlTask task) {
        synchronized (lock) {
            Connection connection = null;
            try {
                connection = DatabaseMain.getConnection();
                connection.setAutoCommit(false);
                task.run();
                connection.commit();
                return true;
            } catch (Exception e) {
                BasicInfo.sendException(e);
                try {
                    if (connection != null) connection.rollback();
                } catch (Exception rollbackError) {
                    BasicInfo.sendException(rollbackError);
                }
                return false;
            } finally {
                try {
                    if (connection != null) connection.setAutoCommit(true);
                } catch (Exception e) {
                    BasicInfo.sendException(e);
                }
            }
        }
    }

    public interface SqlTask {
        void run() throws Exception;
    }

    private static PreparedStatement prepare(String sql,Object[] params) throws Exception {
        Connection connection = DatabaseMain.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        setParams(statement,params);
        return statement;
    }

    private static void setParams(PreparedStatement statement,Object[] params) throws Exception {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i+1,params[i]);
        }
    }

    private static JSONObject readRow(ResultSet set) throws Exception {
        JSONObject row = new JSONObject(true);
        ResultSetMetaData meta = set.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            row.put(meta.getColumnLabel(i),set.getObject(i));
        }
        return row;
    }
}
