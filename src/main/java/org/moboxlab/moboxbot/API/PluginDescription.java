package org.moboxlab.moboxbot.API;

import java.util.ArrayList;
import java.util.List;

/**
 * plugin.json 的解析结果
 */
public class PluginDescription {
    public String name = "";
    public String version = "";
    public String apiVersion = "";
    public String main = "";
    public String author = "";
    public String description = "";
    public String website = "";
    public List<String> depend = new ArrayList<>();
    public List<String> softDepend = new ArrayList<>();
    public List<String> loadBefore = new ArrayList<>();
    public List<String> provides = new ArrayList<>();
    public String filePath = "";
    public String fileName = "";

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isComplete() {
        return name != null && !name.trim().isEmpty()
                && main != null && !main.trim().isEmpty();
    }
}
