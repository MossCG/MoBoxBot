package org.moboxlab.moboxbot.API.Storage;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.Plugin;

import java.util.List;

public interface StorageService {
    String get(Plugin plugin,String key);

    void set(Plugin plugin,String key,String value);

    void remove(Plugin plugin,String key);

    List<JSONObject> query(String sql,Object... params);

    JSONObject queryOne(String sql,Object... params);

    int update(String sql,Object... params);

    long insert(String sql,Object... params);
}
