package org.moboxlab.moboxbot.Plugin;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.Plugin;
import org.moboxlab.moboxbot.API.Storage.StorageService;
import org.moboxlab.moboxbot.Database.SqlExecutor;

import java.util.List;

/**
 * 插件存储适配
 * 小数据走 bot_plugin_data，结构化数据只允许操作 plugin_ 前缀表。
 */
public class StorageServiceImpl implements StorageService {
    @Override
    public String get(Plugin plugin,String key) {
        if (plugin == null || key == null) return null;
        JSONObject row = SqlExecutor.queryOne(
                "SELECT `dataValue`,`expireTime` FROM `bot_plugin_data` WHERE `pluginName`=? AND `dataKey`=?",
                plugin.getName(),key);
        if (row == null) return null;
        long expireTime = row.getLongValue("expireTime");
        if (expireTime > 0 && expireTime < System.currentTimeMillis()) {
            remove(plugin,key);
            return null;
        }
        return row.getString("dataValue");
    }

    @Override
    public void set(Plugin plugin,String key,String value) {
        if (plugin == null || key == null || key.trim().isEmpty()) return;
        long now = System.currentTimeMillis();
        JSONObject exists = SqlExecutor.queryOne(
                "SELECT `ID` FROM `bot_plugin_data` WHERE `pluginName`=? AND `dataKey`=?",
                plugin.getName(),key);
        if (exists == null) {
            SqlExecutor.insert("INSERT INTO `bot_plugin_data` (`pluginName`,`dataKey`,`dataValue`,`expireTime`,`createTime`,`updateTime`) VALUES (?,?,?,?,?,?)",
                    plugin.getName(),key,value == null ? "" : value,0L,now,now);
        } else {
            SqlExecutor.update("UPDATE `bot_plugin_data` SET `dataValue`=?,`updateTime`=? WHERE `pluginName`=? AND `dataKey`=?",
                    value == null ? "" : value,now,plugin.getName(),key);
        }
    }

    @Override
    public void remove(Plugin plugin,String key) {
        if (plugin == null || key == null) return;
        SqlExecutor.update("DELETE FROM `bot_plugin_data` WHERE `pluginName`=? AND `dataKey`=?",plugin.getName(),key);
    }

    @Override
    public List<JSONObject> query(String sql,Object... params) {
        checkSql(sql);
        return SqlExecutor.query(sql,params);
    }

    @Override
    public JSONObject queryOne(String sql,Object... params) {
        checkSql(sql);
        return SqlExecutor.queryOne(sql,params);
    }

    @Override
    public int update(String sql,Object... params) {
        checkSql(sql);
        return SqlExecutor.update(sql,params);
    }

    @Override
    public long insert(String sql,Object... params) {
        checkSql(sql);
        return SqlExecutor.insert(sql,params);
    }

    /**
     * 插件自定义表必须以 plugin_ 开头，并禁止直接操作 bot_ 主程序表。
     */
    private void checkSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) throw new IllegalArgumentException("SQL 不能为空！");
        String lower = sql.toLowerCase();
        if (!lower.contains("plugin_")) throw new IllegalArgumentException("插件 SQL 只允许操作 plugin_ 前缀表！");
        if (lower.contains("bot_")) throw new IllegalArgumentException("插件 SQL 不允许操作 bot_ 主程序表！");
    }
}
