package com.github.cao.awa.sepals.threadpool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SepalsThreadPool {
    public static final SepalsThreadPool INSTANCE = new SepalsThreadPool();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);

    public static Future<?> executeTask(Runnable runnable) {
        return INSTANCE.execute(runnable);
    }

    public Future<?> execute(Runnable runnable) {
        return this.executor.submit(runnable);
    }
}
