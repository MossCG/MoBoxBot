package org.moboxlab.moboxbot.Plugin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.moboxlab.moboxbot.API.MoBoxBotAPI;
import org.moboxlab.moboxbot.API.PluginDescription;
import org.moboxlab.moboxbot.BasicInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * plugin.json 读取器
 */
public class PluginDescriptionReader {
    public static List<PluginDescription> readAll(String dirPath) {
        List<PluginDescription> list = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) return list;
        for (File file : files) {
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".jar")) continue;
            PluginDescription description = read(file);
            if (description != null) list.add(description);
        }
        return list;
    }

    public static PluginDescription read(File file) {
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("plugin.json");
            if (entry == null) {
                BasicInfo.logger.sendWarn("插件 JAR 里没有 plugin.json，已跳过："+file.getName());
                return null;
            }
            InputStream input = jar.getInputStream(entry);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int read;
            while ((read = input.read(buffer)) > 0) output.write(buffer,0,read);
            input.close();
            JSONObject json = JSONObject.parseObject(new String(output.toByteArray(),StandardCharsets.UTF_8));
            if (json == null) {
                BasicInfo.logger.sendWarn("plugin.json 不是合法 JSON，已跳过："+file.getName());
                return null;
            }
            return parse(json,file);
        } catch (Exception e) {
            BasicInfo.logger.sendWarn("读取插件失败："+file.getName()+"，原因："+e.getMessage());
            return null;
        } finally {
            try {
                if (jar != null) jar.close();
            } catch (Exception e) {
                BasicInfo.sendException(e);
            }
        }
    }

    private static PluginDescription parse(JSONObject json,File file) {
        PluginDescription description = new PluginDescription();
        description.name = text(json,"name");
        description.version = text(json,"version");
        description.apiVersion = text(json,"apiVersion");
        description.main = text(json,"main");
        description.author = text(json,"author");
        description.description = text(json,"description");
        description.website = text(json,"website");
        description.depend = list(json,"depend");
        description.softDepend = list(json,"softDepend");
        description.loadBefore = list(json,"loadBefore");
        description.provides = list(json,"provides");
        description.filePath = file.getAbsolutePath();
        description.fileName = file.getName();
        if (!description.isComplete()) {
            BasicInfo.logger.sendWarn("插件 "+file.getName()+" 缺少 name 或 main 字段，已跳过！");
            return null;
        }
        if (!checkApiVersion(description)) return null;
        return description;
    }

    private static boolean checkApiVersion(PluginDescription description) {
        String required = description.apiVersion;
        if (required == null || required.trim().isEmpty()) {
            BasicInfo.logger.sendWarn("插件 "+description.name+" 没有声明 apiVersion，已跳过！");
            return false;
        }
        String current = MoBoxBotAPI.API_VERSION;
        if (!mainVersion(required).equals(mainVersion(current))) {
            BasicInfo.logger.sendWarn("插件 "+description.name+" 需要 API "+required
                    +"，当前主程序是 "+current+"，主版本不一致，已跳过！");
            return false;
        }
        if (compare(required,current) > 0) {
            BasicInfo.logger.sendWarn("插件 "+description.name+" 声明的 API 版本 "+required
                    +" 高于主程序 "+current+"，可能缺少接口！");
        }
        return true;
    }

    private static String mainVersion(String version) {
        int index = version.indexOf('.');
        return index < 0 ? version : version.substring(0,index);
    }

    private static int compare(String left,String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int length = Math.max(leftParts.length,rightParts.length);
        for (int i = 0; i < length; i++) {
            int leftValue = i < leftParts.length ? parseInt(leftParts[i]) : 0;
            int rightValue = i < rightParts.length ? parseInt(rightParts[i]) : 0;
            if (leftValue != rightValue) return leftValue - rightValue;
        }
        return 0;
    }

    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String text(JSONObject json,String key) {
        String value = json.getString(key);
        return value == null ? "" : value.trim();
    }

    private static List<String> list(JSONObject json,String key) {
        List<String> result = new ArrayList<>();
        Object value = json.get(key);
        if (value == null) return result;
        if (value instanceof JSONArray) {
            for (Object object : (JSONArray) value) {
                if (object == null) continue;
                String text = String.valueOf(object).trim();
                if (!text.isEmpty()) result.add(text);
            }
            return result;
        }
        for (String part : String.valueOf(value).split(",")) {
            String trim = part.trim();
            if (!trim.isEmpty()) result.add(trim);
        }
        return result;
    }
}
