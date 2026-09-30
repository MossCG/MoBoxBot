package org.moboxlab.moboxbot.API.Event;

/**
 * 所有事件的基类
 */
public abstract class Event {
    private boolean cancelled = false;

    public abstract String getName();

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
