package org.moboxlab.moboxbot.API;

import org.moboxlab.moboxbot.API.Util.PluginConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 插件基类
 * 生命周期：onLoad → onEnable → onDisable
 */
public abstract class Plugin {
    private PluginDescription description;
    private PluginLogger logger;
    private Server server;
    private String dataFolder;
    private PluginConfig config;
    private boolean enabled = false;

    public void initPlugin(Server pluginServer,PluginDescription pluginDescription,PluginLogger pluginLogger,String pluginDataFolder) {
        this.server = pluginServer;
        this.description = pluginDescription;
        this.logger = pluginLogger;
        this.dataFolder = pluginDataFolder;
    }

    public void setEnabled(boolean pluginEnabled) {
        this.enabled = pluginEnabled;
    }

    public PluginDescription getDescription() {
        return description;
    }

    public String getName() {
        return description == null ? "" : description.name;
    }

    public String getVersion() {
        return description == null ? "" : description.version;
    }

    public PluginLogger getLogger() {
        return logger;
    }

    public Server getServer() {
        return server;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public PluginConfig getConfig() {
        if (config == null) config = new PluginConfig(dataFolder+"/config.yml");
        return config;
    }

    public boolean saveDefaultConfig() {
        String path = dataFolder+"/config.yml";
        if (config == null) config = new PluginConfig(path);
        if (new File(path).exists()) {
            config.load();
            return true;
        }
        try {
            new File(dataFolder).mkdirs();
            byte[] bytes = readResource("config.yml");
            if (bytes == null) {
                if (logger != null) logger.sendWarn("插件 JAR 里没有 config.yml，跳过默认配置释放！");
                return false;
            }
            Files.write(Paths.get(path),bytes);
            config.load();
            return true;
        } catch (Exception e) {
            if (logger != null) logger.sendException(e);
            return false;
        }
    }

    public byte[] readResource(String entryName) {
        String jarPath = description == null ? "" : description.filePath;
        if (jarPath == null || jarPath.isEmpty()) return null;
        JarFile jar = null;
        try {
            jar = new JarFile(jarPath);
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry == null) return null;
            InputStream input = jar.getInputStream(entry);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int read;
            while ((read = input.read(buffer)) > 0) output.write(buffer,0,read);
            input.close();
            return output.toByteArray();
        } catch (Exception e) {
            if (logger != null) logger.sendWarn("读取插件资源失败："+entryName+"，原因："+e.getMessage());
            return null;
        } finally {
            try {
                if (jar != null) jar.close();
            } catch (Exception ignored) {
            }
        }
    }

    public String readResourceText(String entryName) {
        byte[] bytes = readResource(entryName);
        if (bytes == null) return "";
        try {
            return new String(bytes,"UTF-8");
        } catch (Exception e) {
            if (logger != null) logger.sendException(e);
            return "";
        }
    }

    public void onLoad() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
