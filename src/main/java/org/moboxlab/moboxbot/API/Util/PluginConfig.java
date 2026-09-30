package org.moboxlab.moboxbot.API.Util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 插件配置（键值对 YAML 子集）
 * 写回时逐行替换，保留注释、空行与顺序。
 */
public class PluginConfig {
    private final String path;
    private final Map<String,String> values = new LinkedHashMap<>();
    private final List<String> lines = new ArrayList<>();
    private final List<String> changedKeys = new ArrayList<>();

    public PluginConfig(String path) {
        this.path = path;
        load();
    }

    public String getPath() {
        return path;
    }

    public synchronized boolean load() {
        values.clear();
        lines.clear();
        changedKeys.clear();
        try {
            if (!Files.exists(Paths.get(path))) return false;
            String text = new String(Files.readAllBytes(Paths.get(path)),StandardCharsets.UTF_8);
            for (String line : text.split("\n",-1)) {
                String keep = line.endsWith("\r") ? line.substring(0,line.length() - 1) : line;
                lines.add(keep);
                String key = keyOf(keep);
                if (key != null) values.put(key,valueOf(keep));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized boolean save() {
        try {
            for (String key : changedKeys) {
                String newLine = key+": "+formatValue(values.get(key));
                boolean replaced = false;
                for (int i = 0; i < lines.size(); i++) {
                    if (key.equals(keyOf(lines.get(i)))) {
                        lines.set(i,newLine);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) lines.add(newLine);
            }
            changedKeys.clear();
            StringBuilder builder = new StringBuilder();
            for (String line : lines) builder.append(line).append("\n");
            Files.write(Paths.get(path),builder.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getString(String key) {
        return getString(key,null);
    }

    public String getString(String key,String defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : value;
    }

    public int getInt(String key,int defaultValue) {
        try {
            return Integer.parseInt(getString(key,String.valueOf(defaultValue)).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public long getLong(String key,long defaultValue) {
        try {
            return Long.parseLong(getString(key,String.valueOf(defaultValue)).trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key,boolean defaultValue) {
        String value = getString(key,String.valueOf(defaultValue)).trim().toLowerCase();
        if ("true".equals(value) || "1".equals(value) || "yes".equals(value)) return true;
        if ("false".equals(value) || "0".equals(value) || "no".equals(value)) return false;
        return defaultValue;
    }

    public void set(String key,String value) {
        if (key == null || key.trim().isEmpty()) return;
        values.put(key,value);
        if (!changedKeys.contains(key)) changedKeys.add(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public static String formatValue(String value) {
        String trim = value == null ? "" : value.trim();
        if ("true".equals(trim) || "false".equals(trim)) return trim;
        if (trim.matches("-?\\d+")) return trim;
        return "\""+trim.replace("\\","\\\\").replace("\"","\\\"")+"\"";
    }

    private static String keyOf(String line) {
        String trim = line == null ? "" : line.trim();
        if (trim.isEmpty() || trim.startsWith("#")) return null;
        int index = trim.indexOf(':');
        if (index <= 0) return null;
        String key = trim.substring(0,index).trim();
        if (!key.matches("[A-Za-z0-9_.\\-]+")) return null;
        return key;
    }

    private static String valueOf(String line) {
        String trim = line == null ? "" : line.trim();
        int index = trim.indexOf(':');
        if (index <= 0) return "";
        String value = trim.substring(index + 1).trim();
        int comment = indexOfComment(value);
        if (comment >= 0) value = value.substring(0,comment).trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1,value.length() - 1).replace("\\\"","\"").replace("\\\\","\\");
        }
        return value;
    }

    private static int indexOfComment(String value) {
        boolean inQuote = false;
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '"') inQuote = !inQuote;
            if (character == '#' && !inQuote) return i;
        }
        return -1;
    }
}
