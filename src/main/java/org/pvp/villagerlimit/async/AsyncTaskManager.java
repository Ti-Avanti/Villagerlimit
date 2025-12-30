package org.pvp.villagerlimit.async;

import org.bukkit.scheduler.BukkitTask;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 异步任务管理器
 * 统一管理所有异步任务，提供线程池支持
 */
public class AsyncTaskManager extends AbstractModule {
    
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutor;
    private final List<BukkitTask> bukkitTasks;
    
    public AsyncTaskManager(Villagerlimit plugin) {
        super(plugin);
        this.bukkitTasks = new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "AsyncTaskManager";
    }
    
    @Override
    public void onEnable() {
        // 创建线程池
        int corePoolSize = plugin.getConfig().getInt("async.core-pool-size", 4);
        int maxPoolSize = plugin.getConfig().getInt("async.max-pool-size", 8);
        
        this.executorService = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "VillagerLimit-Async-" + counter++);
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "VillagerLimit-Scheduled-" + counter++);
                thread.setDaemon(true);
                return thread;
            }
        });
        
        info("异步任务管理器已启用，核心线程数: " + corePoolSize);
    }
    
    @Override
    public void onDisable() {
        // 取消所有Bukkit任务
        bukkitTasks.forEach(BukkitTask::cancel);
        bukkitTasks.clear();
        
        // 关闭线程池
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
            }
        }
        
        info("异步任务管理器已关闭");
    }
    
    /**
     * 提交异步任务
     */
    public <T> CompletableFuture<T> submitAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                error("异步任务执行失败: " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }
    
    /**
     * 提交异步任务（无返回值）
     */
    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                error("异步任务执行失败: " + e.getMessage());
            }
        }, executorService);
    }
    
    /**
     * 延迟执行异步任务
     */
    public ScheduledFuture<?> scheduleAsync(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }
    
    /**
     * 定时执行异步任务
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * 在主线程执行任务
     */
    public void runSync(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }
    
    /**
     * 延迟在主线程执行任务
     */
    public BukkitTask runSyncLater(Runnable task, long delay) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
    }
    
    /**
     * 定时在主线程执行任务
     */
    public BukkitTask runSyncTimer(Runnable task, long delay, long period) {
        BukkitTask bukkitTask = plugin.getServer().getScheduler().runTaskTimer(plugin, task, delay, period);
        bukkitTasks.add(bukkitTask);
        return bukkitTask;
    }
    
    /**
     * 批量执行异步任务
     */
    public <T> CompletableFuture<List<T>> batchAsync(List<Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submitAsync(task));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }
    
    /**
     * 异步执行后在主线程回调
     */
    public <T> void asyncThenSync(Callable<T> asyncTask, Consumer<T> syncCallback) {
        submitAsync(asyncTask).thenAccept(result -> {
            runSync(() -> syncCallback.accept(result));
        });
    }
    
    /**
     * 获取线程池统计信息
     */
    public ThreadPoolStats getStats() {
        if (executorService instanceof ThreadPoolExecutor tpe) {
            return new ThreadPoolStats(
                tpe.getActiveCount(),
                tpe.getPoolSize(),
                tpe.getQueue().size(),
                tpe.getCompletedTaskCount()
            );
        }
        return new ThreadPoolStats(0, 0, 0, 0);
    }
    
    /**
     * 线程池统计信息
     */
    public static class ThreadPoolStats {
        public final int activeThreads;
        public final int poolSize;
        public final int queueSize;
        public final long completedTasks;
        
        public ThreadPoolStats(int activeThreads, int poolSize, int queueSize, long completedTasks) {
            this.activeThreads = activeThreads;
            this.poolSize = poolSize;
            this.queueSize = queueSize;
            this.completedTasks = completedTasks;
        }
    }
}
