package org.moboxlab.moboxbot.Command;

import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxlib.Object.ObjectCommand;
import org.moboxlab.moboxlib.Object.ObjectLogger;

import java.util.ArrayList;
import java.util.List;

public class CommandDebug extends ObjectCommand {
    @Override
    public List<String> prefix() {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("debug");
        return prefixList;
    }

    @Override
    public boolean execute(String[] args,ObjectLogger logger) {
        BasicInfo.debug = !BasicInfo.debug;
        BasicInfo.logger.sendInfo("调试模式已"+(BasicInfo.debug ? "开启" : "关闭")+"！");
        return true;
    }
}
