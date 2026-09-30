package org.moboxlab.moboxbot.Task;

import org.moboxlab.moboxbot.BasicInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务框架
 */
public class SchedulerService {
    private static ScheduledExecutorService pool;

    public static void start() {
        pool = Executors.newScheduledThreadPool(4,runnable -> {
            Thread thread = new Thread(runnable,"MoBoxBot-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        BasicInfo.logger.sendInfo("定时任务框架已启动！");
    }

    public static ScheduledFuture<?> runTaskLater(Runnable task,long delaySeconds) {
        if (pool == null) return null;
        return pool.schedule(wrap(task),delaySeconds,TimeUnit.SECONDS);
    }

    public static ScheduledFuture<?> runTaskTimer(Runnable task,long delaySeconds,long periodSeconds) {
        if (pool == null) return null;
        return pool.scheduleAtFixedRate(wrap(task),delaySeconds,periodSeconds,TimeUnit.SECONDS);
    }

    public static Future<?> runTaskAsync(Runnable task) {
        if (pool == null) return null;
        return pool.submit(wrap(task));
    }

    public static void stop() {
        if (pool == null) return;
        pool.shutdownNow();
        pool = null;
    }

    private static Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                BasicInfo.sendException(throwable);
            }
        };
    }
}
