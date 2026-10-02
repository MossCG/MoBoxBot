package org.moboxlab.moboxbot.API;

import com.alibaba.fastjson.JSONObject;

/**
 * 插件公开服务
 * 其他插件通过服务名调用，不直接依赖服务提供方的实现类。
 */
public interface PluginService {
    String getName();

    JSONObject call(String action,JSONObject params);
}
