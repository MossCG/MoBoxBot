package org.moboxlab.moboxbot.API.Event;

import org.moboxlab.moboxbot.API.Plugin;

public interface EventBus {
    void register(Plugin plugin,Listener listener);

    void unregister(Plugin plugin,Listener listener);

    void unregisterAll(Plugin plugin);

    Event callEvent(Event event);

    void callEventAsync(Event event);
}
