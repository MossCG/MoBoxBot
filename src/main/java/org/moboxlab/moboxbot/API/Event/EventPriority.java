package org.moboxlab.moboxbot.API.Event;

/**
 * MONITOR 只用于观察与记录，不要在里面改事件或业务数据。
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR
}
