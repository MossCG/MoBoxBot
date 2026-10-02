package org.moboxlab.moboxbot.API.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令元数据，供 help 等插件展示。
 */
public class CommandInfo {
    public String name = "";
    public List<String> aliases = new ArrayList<>();
    public String description = "";
    public CommandPermission permission = CommandPermission.EVERYONE;
    public String source = "";
    public List<String> usages = new ArrayList<>();

    public CommandInfo() {
    }

    public CommandInfo(String name,List<String> aliases,String description,CommandPermission permission,String source) {
        this(name,aliases,description,permission,source,null);
    }

    public CommandInfo(String name,List<String> aliases,String description,CommandPermission permission,String source,List<String> usages) {
        this.name = name == null ? "" : name;
        this.aliases = aliases == null ? new ArrayList<String>() : aliases;
        this.description = description == null ? "" : description;
        this.permission = permission == null ? CommandPermission.EVERYONE : permission;
        this.source = source == null ? "" : source;
        this.usages = usages == null ? new ArrayList<String>() : usages;
    }
}
