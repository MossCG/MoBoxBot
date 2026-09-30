package org.moboxlab.moboxbot.Plugin;

import org.moboxlab.moboxbot.API.Event.Event;
import org.moboxlab.moboxbot.API.Event.EventBus;
import org.moboxlab.moboxbot.API.Event.EventHandler;
import org.moboxlab.moboxbot.API.Event.Listener;
import org.moboxlab.moboxbot.API.Plugin;
import org.moboxlab.moboxbot.BasicInfo;
import org.moboxlab.moboxbot.Task.SchedulerService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 事件总线
 * 反射扫描 @EventHandler，默认同步分发。
 */
public class EventBusImpl implements EventBus {
    private final List<Handler> handlerList = new ArrayList<>();

    private static class Handler {
        private final Plugin plugin;
        private final Listener listener;
        private final Method method;
        private final Class<?> eventType;
        private final int priority;
        private final boolean ignoreCancelled;

        private Handler(Plugin plugin,Listener listener,Method method,Class<?> eventType,int priority,boolean ignoreCancelled) {
            this.plugin = plugin;
            this.listener = listener;
            this.method = method;
            this.eventType = eventType;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
        }
    }

    @Override
    public synchronized void register(Plugin plugin,Listener listener) {
        if (listener == null) return;
        int count = 0;
        for (Method method : listener.getClass().getMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) continue;
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 1 || !Event.class.isAssignableFrom(parameters[0])) {
                BasicInfo.logger.sendWarn("插件 "+pluginName(plugin)+" 的监听方法 "+method.getName()
                        +" 必须有且只有一个事件参数，已忽略！");
                continue;
            }
            handlerList.add(new Handler(plugin,listener,method,parameters[0],
                    annotation.priority().ordinal(),annotation.ignoreCancelled()));
            count++;
        }
        if (count <= 0) {
            BasicInfo.logger.sendWarn("插件 "+pluginName(plugin)+" 注册的监听器里没有找到 @EventHandler 方法！");
            return;
        }
        handlerList.sort((left,right) -> Integer.compare(left.priority,right.priority));
        BasicInfo.logger.sendInfo("插件 "+pluginName(plugin)+" 注册了 "+count+" 个事件监听方法！");
    }

    @Override
    public synchronized void unregister(Plugin plugin,Listener listener) {
        Iterator<Handler> iterator = handlerList.iterator();
        while (iterator.hasNext()) {
            Handler handler = iterator.next();
            if (handler.plugin == plugin && handler.listener == listener) iterator.remove();
        }
    }

    @Override
    public synchronized void unregisterAll(Plugin plugin) {
        if (plugin == null) return;
        Iterator<Handler> iterator = handlerList.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Handler handler = iterator.next();
            if (handler.plugin == plugin) {
                iterator.remove();
                count++;
            }
        }
        if (count > 0) BasicInfo.logger.sendInfo("已注销插件 "+plugin.getName()+" 的 "+count+" 个监听方法！");
    }

    @Override
    public Event callEvent(Event event) {
        if (event == null) return null;
        List<Handler> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(handlerList);
        }
        for (Handler handler : snapshot) {
            if (!handler.eventType.isInstance(event)) continue;
            if (event.isCancelled() && handler.ignoreCancelled) continue;
            try {
                handler.method.invoke(handler.listener,event);
            } catch (InvocationTargetException e) {
                BasicInfo.logger.sendWarn("插件 "+pluginName(handler.plugin)+" 处理事件 "+event.getName()
                        +" 时抛出异常："+e.getTargetException());
                BasicInfo.sendException(e.getTargetException());
            } catch (Exception e) {
                BasicInfo.sendException(e);
            }
        }
        return event;
    }

    @Override
    public void callEventAsync(Event event) {
        if (event == null) return;
        SchedulerService.runTaskAsync(() -> callEvent(event));
    }

    public synchronized int count(Plugin plugin) {
        int count = 0;
        for (Handler handler : handlerList) {
            if (handler.plugin == plugin) count++;
        }
        return count;
    }

    private static String pluginName(Plugin plugin) {
        return plugin == null ? "未知插件" : plugin.getName();
    }
}
