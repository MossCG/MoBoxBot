package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.Main;
import org.moboxlab.moboxlib.Object.ObjectCommand;
import org.moboxlab.moboxlib.Object.ObjectLogger;

import java.util.ArrayList;
import java.util.List;

public class CommandReload extends ObjectCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("reload");
        return prefixList;
    }

    @Override
    public boolean execute(String[] args,ObjectLogger logger) {
        Main.reloadConfig();
        return true;
    }
}
