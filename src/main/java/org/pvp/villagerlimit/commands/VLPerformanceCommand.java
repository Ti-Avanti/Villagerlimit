package org.pvp.villagerlimit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.async.AsyncTaskManager;
import org.pvp.villagerlimit.cache.CacheManager;
import org.pvp.villagerlimit.monitoring.PerformanceMonitor;
import org.pvp.villagerlimit.optimization.MemoryOptimizer;
import org.pvp.villagerlimit.optimization.VillagerChunkTracker;

/**
 * 性能监控命令
 * /vlperf - 查看性能统计
 */
public class VLPerformanceCommand implements CommandExecutor {
    
    private final Villagerlimit plugin;
    
    public VLPerformanceCommand(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.admin")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        if (args.length == 0) {
            showPerformanceStats(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "cache" -> showCacheStats(sender);
            case "memory" -> showMemoryStats(sender);
            case "threads" -> showThreadStats(sender);
            case "tracker" -> showTrackerStats(sender);
            case "cleanup" -> performCleanup(sender);
            default -> {
                sender.sendMessage("§c用法: /vlperf [cache|memory|threads|tracker|cleanup]");
                return true;
            }
        }
        
        return true;
    }
    
    /**
     * 显示性能统计
     */
    private void showPerformanceStats(CommandSender sender) {
        PerformanceMonitor monitor = plugin.getModuleManager().getModule(PerformanceMonitor.class);
        if (monitor == null) {
            sender.sendMessage("§c性能监控器未启用！");
            return;
        }
        
        PerformanceMonitor.PerformanceReport report = monitor.generateReport();
        
        sender.sendMessage("§6========== VillagerLimit 性能统计 ==========");
        sender.sendMessage("§eTPS: §f" + String.format("%.2f", report.avgTPS));
        sender.sendMessage("§e内存使用: §f" + report.usedMemoryMB + "MB / " + report.maxMemoryMB + "MB §7(" + String.format("%.2f%%", report.memoryUsagePercent) + ")");
        sender.sendMessage("§e平均任务时间: §f" + String.format("%.2f", report.avgTaskTime) + "ms");
        sender.sendMessage("§e任务记录数: §f" + report.taskCount);
        sender.sendMessage("§6==========================================");
    }
    
    /**
     * 显示缓存统计
     */
    private void showCacheStats(CommandSender sender) {
        CacheManager cacheManager = plugin.getModuleManager().getModule(CacheManager.class);
        if (cacheManager == null) {
            sender.sendMessage("§c缓存管理器未启用！");
            return;
        }
        
        CacheManager.CacheStats stats = cacheManager.getStats();
        
        sender.sendMessage("§6========== 缓存统计 ==========");
        sender.sendMessage("§e玩家数据缓存: §f" + stats.playerDataCount);
        sender.sendMessage("§e权限组缓存: §f" + stats.permissionGroupCount);
        sender.sendMessage("§e缓存过期时间: §f" + (stats.expireTime / 1000) + "秒");
        sender.sendMessage("§6============================");
    }
    
    /**
     * 显示内存统计
     */
    private void showMemoryStats(CommandSender sender) {
        MemoryOptimizer optimizer = plugin.getModuleManager().getModule(MemoryOptimizer.class);
        if (optimizer == null) {
            sender.sendMessage("§c内存优化器未启用！");
            return;
        }
        
        MemoryOptimizer.MemoryStats stats = optimizer.getMemoryStats();
        
        sender.sendMessage("§6========== 内存统计 ==========");
        sender.sendMessage("§e最大内存: §f" + stats.maxMemoryMB + "MB");
        sender.sendMessage("§e已分配内存: §f" + stats.totalMemoryMB + "MB");
        sender.sendMessage("§e已使用内存: §f" + stats.usedMemoryMB + "MB §7(" + String.format("%.2f%%", stats.getUsagePercent()) + ")");
        sender.sendMessage("§e空闲内存: §f" + stats.freeMemoryMB + "MB");
        sender.sendMessage("§e弱引用缓存: §f" + stats.weakCacheSize);
        sender.sendMessage("§6============================");
    }
    
    /**
     * 显示线程池统计
     */
    private void showThreadStats(CommandSender sender) {
        AsyncTaskManager taskManager = plugin.getModuleManager().getModule(AsyncTaskManager.class);
        if (taskManager == null) {
            sender.sendMessage("§c异步任务管理器未启用！");
            return;
        }
        
        AsyncTaskManager.ThreadPoolStats stats = taskManager.getStats();
        
        sender.sendMessage("§6========== 线程池统计 ==========");
        sender.sendMessage("§e活跃线程: §f" + stats.activeThreads);
        sender.sendMessage("§e线程池大小: §f" + stats.poolSize);
        sender.sendMessage("§e队列大小: §f" + stats.queueSize);
        sender.sendMessage("§e已完成任务: §f" + stats.completedTasks);
        sender.sendMessage("§6==============================");
    }
    
    /**
     * 显示追踪器统计
     */
    private void showTrackerStats(CommandSender sender) {
        VillagerChunkTracker tracker = plugin.getModuleManager().getModule(VillagerChunkTracker.class);
        if (tracker == null) {
            sender.sendMessage("§c村民追踪器未启用！");
            return;
        }
        
        VillagerChunkTracker.TrackerStats stats = tracker.getStats();
        
        sender.sendMessage("§6========== 村民追踪统计 ==========");
        sender.sendMessage("§e追踪区块数: §f" + stats.trackedChunks);
        sender.sendMessage("§e总村民数: §f" + stats.totalVillagers);
        sender.sendMessage("§6================================");
    }
    
    /**
     * 执行清理
     */
    private void performCleanup(CommandSender sender) {
        sender.sendMessage("§e正在执行清理...");
        
        // 清理缓存
        CacheManager cacheManager = plugin.getModuleManager().getModule(CacheManager.class);
        if (cacheManager != null) {
            cacheManager.clearAll();
        }
        
        // 清理内存
        MemoryOptimizer optimizer = plugin.getModuleManager().getModule(MemoryOptimizer.class);
        if (optimizer != null) {
            optimizer.manualCleanup();
        }
        
        sender.sendMessage("§a清理完成！");
    }
}
