package org.pvp.villagerlimit.monitoring;

import org.bukkit.Bukkit;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * 性能监控器
 * 监控TPS、内存使用、任务执行时间等
 */
public class PerformanceMonitor extends AbstractModule {
    
    // TPS历史记录（最近60秒）
    private final Deque<Double> tpsHistory;
    
    // 任务执行时间记录
    private final Deque<TaskRecord> taskRecords;
    
    // 最大记录数
    private static final int MAX_RECORDS = 60;
    
    // 上次tick时间
    private long lastTick;
    
    public PerformanceMonitor(Villagerlimit plugin) {
        super(plugin);
        this.tpsHistory = new ArrayDeque<>(MAX_RECORDS);
        this.taskRecords = new ArrayDeque<>(MAX_RECORDS);
        this.lastTick = System.currentTimeMillis();
    }
    
    @Override
    public String getName() {
        return "PerformanceMonitor";
    }
    
    @Override
    public void onEnable() {
        // 启动TPS监控任务
        startTPSMonitoring();
        
        // 启动性能报告任务
        startPerformanceReporting();
        
        info("性能监控器已启用");
    }
    
    @Override
    public void onDisable() {
        tpsHistory.clear();
        taskRecords.clear();
        info("性能监控器已关闭");
    }
    
    /**
     * 启动TPS监控
     */
    private void startTPSMonitoring() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double tps = getCurrentTPS();
            
            synchronized (tpsHistory) {
                if (tpsHistory.size() >= MAX_RECORDS) {
                    tpsHistory.removeFirst();
                }
                tpsHistory.addLast(tps);
            }
            
            lastTick = System.currentTimeMillis();
        }, 20L, 20L); // 每秒执行一次
    }
    
    /**
     * 启动性能报告
     */
    private void startPerformanceReporting() {
        int reportInterval = plugin.getConfig().getInt("monitoring.report-interval", 300);
        
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (plugin.getConfig().getBoolean("monitoring.auto-report", false)) {
                PerformanceReport report = generateReport();
                
                // 如果TPS过低，发出警告
                if (report.avgTPS < 18.0) {
                    warn("服务器TPS过低: " + String.format("%.2f", report.avgTPS));
                }
                
                // 如果内存使用过高，发出警告
                if (report.memoryUsagePercent > 90) {
                    warn("内存使用过高: " + String.format("%.2f%%", report.memoryUsagePercent));
                }
            }
        }, 20L * reportInterval, 20L * reportInterval);
    }
    
    /**
     * 获取当前TPS
     */
    private double getCurrentTPS() {
        try {
            // 使用反射获取服务器TPS
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            return recentTps[0];
        } catch (Exception e) {
            // 如果反射失败，使用估算值
            return 20.0;
        }
    }
    
    /**
     * 记录任务执行时间
     */
    public void recordTask(String taskName, long executionTime) {
        synchronized (taskRecords) {
            if (taskRecords.size() >= MAX_RECORDS) {
                taskRecords.removeFirst();
            }
            taskRecords.addLast(new TaskRecord(taskName, executionTime, System.currentTimeMillis()));
        }
        
        // 如果执行时间过长，发出警告
        if (executionTime > 50) {
            warn("任务执行时间过长: " + taskName + " (" + executionTime + "ms)");
        }
    }
    
    /**
     * 测量任务执行时间
     */
    public <T> T measureTask(String taskName, java.util.function.Supplier<T> task) {
        long start = System.nanoTime();
        try {
            return task.get();
        } finally {
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            recordTask(taskName, duration);
        }
    }
    
    /**
     * 生成性能报告
     */
    public PerformanceReport generateReport() {
        // 计算平均TPS
        double avgTPS;
        synchronized (tpsHistory) {
            avgTPS = tpsHistory.stream().mapToDouble(Double::doubleValue).average().orElse(20.0);
        }
        
        // 获取内存信息
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        // 计算平均任务执行时间
        double avgTaskTime;
        synchronized (taskRecords) {
            avgTaskTime = taskRecords.stream()
                .mapToLong(r -> r.executionTime)
                .average()
                .orElse(0.0);
        }
        
        return new PerformanceReport(
            avgTPS,
            usedMemory / 1024 / 1024,
            maxMemory / 1024 / 1024,
            memoryUsagePercent,
            avgTaskTime,
            taskRecords.size()
        );
    }
    
    /**
     * 获取TPS历史
     */
    public Deque<Double> getTPSHistory() {
        synchronized (tpsHistory) {
            return new ArrayDeque<>(tpsHistory);
        }
    }
    
    /**
     * 获取任务记录
     */
    public Deque<TaskRecord> getTaskRecords() {
        synchronized (taskRecords) {
            return new ArrayDeque<>(taskRecords);
        }
    }
    
    /**
     * 任务记录
     */
    public static class TaskRecord {
        public final String taskName;
        public final long executionTime;
        public final long timestamp;
        
        public TaskRecord(String taskName, long executionTime, long timestamp) {
            this.taskName = taskName;
            this.executionTime = executionTime;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * 性能报告
     */
    public static class PerformanceReport {
        public final double avgTPS;
        public final long usedMemoryMB;
        public final long maxMemoryMB;
        public final double memoryUsagePercent;
        public final double avgTaskTime;
        public final int taskCount;
        
        public PerformanceReport(double avgTPS, long usedMemoryMB, long maxMemoryMB, 
                                double memoryUsagePercent, double avgTaskTime, int taskCount) {
            this.avgTPS = avgTPS;
            this.usedMemoryMB = usedMemoryMB;
            this.maxMemoryMB = maxMemoryMB;
            this.memoryUsagePercent = memoryUsagePercent;
            this.avgTaskTime = avgTaskTime;
            this.taskCount = taskCount;
        }
        
        @Override
        public String toString() {
            return String.format(
                "TPS: %.2f | 内存: %dMB/%dMB (%.2f%%) | 平均任务时间: %.2fms | 任务数: %d",
                avgTPS, usedMemoryMB, maxMemoryMB, memoryUsagePercent, avgTaskTime, taskCount
            );
        }
    }
}
