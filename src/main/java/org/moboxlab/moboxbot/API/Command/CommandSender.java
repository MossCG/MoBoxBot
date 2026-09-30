package org.moboxlab.moboxbot.API.Command;

public interface CommandSender {
    boolean isGroup();

    long getGroupID();

    long getUserID();

    String getName();

    String getCommandPrefix();

    boolean hasPermission(CommandPermission permission);

    void sendMessage(String message);

    void reply(String message);
}
