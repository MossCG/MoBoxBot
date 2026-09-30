package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.Main;
import org.moboxlab.moboxlib.Object.ObjectCommand;
import org.moboxlab.moboxlib.Object.ObjectLogger;

import java.util.ArrayList;
import java.util.List;

public class CommandExit extends ObjectCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("exit");
        prefixList.add("stop");
        return prefixList;
    }

    @Override
    public boolean execute(String[] args,ObjectLogger logger) {
        Main.shutdown();
        return true;
    }
}
