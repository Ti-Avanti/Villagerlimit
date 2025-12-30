package org.pvp.villagerlimit.optimization;

import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存优化器
 * 定时清理过期数据，使用弱引用缓存
 */
public class MemoryOptimizer extends AbstractModule {
    
    // 弱引用缓存池
    private final Map<String, WeakReference<Object>> weakCache;
    
    // 清理间隔（秒）
    private int cleanupInterval;
    
    public MemoryOptimizer(Villagerlimit plugin) {
        super(plugin);
        this.weakCache = new ConcurrentHashMap<>();
    }
    
    @Override
    public String getName() {
        return "MemoryOptimizer";
    }
    
    @Override
    public void onEnable() {
        cleanupInterval = plugin.getConfig().getInt("memory.cleanup-interval", 600);
        
        // 启动定时清理任务
        startCleanupTask();
        
        info("内存优化器已启用，清理间隔: " + cleanupInterval + "秒");
    }
    
    @Override
    public void onDisable() {
        weakCache.clear();
        info("内存优化器已关闭");
    }
    
    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                cleanupExpiredData();
                cleanupWeakCache();
                
                // 建议JVM进行垃圾回收（仅建议，不强制）
                if (plugin.getConfig().getBoolean("memory.suggest-gc", false)) {
                    System.gc();
                }
            } catch (Exception e) {
                error("内存清理失败: " + e.getMessage());
            }
        }, 20L * cleanupInterval, 20L * cleanupInterval);
    }
    
    /**
     * 清理过期数据
     */
    private void cleanupExpiredData() {
        // 清理过期的交易限制记录
        String today = LocalDate.now().toString();
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 删除旧日期的交易限制记录
                String sql = "DELETE FROM trade_limits WHERE reset_date < ?";
                plugin.getModuleManager().getModule(org.pvp.villagerlimit.database.DatabaseManager.class)
                    .executeUpdate(sql, today);
                
                // 删除过期的冷却记录
                long now = System.currentTimeMillis();
                sql = "DELETE FROM trade_cooldowns WHERE cooldown_end < ?";
                plugin.getModuleManager().getModule(org.pvp.villagerlimit.database.DatabaseManager.class)
                    .executeUpdate(sql, now);
                
            } catch (Exception e) {
                error("清理过期数据失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 清理弱引用缓存
     */
    private void cleanupWeakCache() {
        weakCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }
    
    /**
     * 添加到弱引用缓存
     */
    public void putWeakCache(String key, Object value) {
        weakCache.put(key, new WeakReference<>(value));
    }
    
    /**
     * 从弱引用缓存获取
     */
    public Object getWeakCache(String key) {
        WeakReference<Object> ref = weakCache.get(key);
        return ref != null ? ref.get() : null;
    }
    
    /**
     * 获取内存使用情况
     */
    public MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return new MemoryStats(
            maxMemory / 1024 / 1024,
            totalMemory / 1024 / 1024,
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024,
            weakCache.size()
        );
    }
    
    /**
     * 手动触发清理
     */
    public void manualCleanup() {
        cleanupExpiredData();
        cleanupWeakCache();
        info("手动清理完成");
    }
    
    /**
     * 内存统计信息
     */
    public static class MemoryStats {
        public final long maxMemoryMB;
        public final long totalMemoryMB;
        public final long usedMemoryMB;
        public final long freeMemoryMB;
        public final int weakCacheSize;
        
        public MemoryStats(long maxMemoryMB, long totalMemoryMB, long usedMemoryMB, long freeMemoryMB, int weakCacheSize) {
            this.maxMemoryMB = maxMemoryMB;
            this.totalMemoryMB = totalMemoryMB;
            this.usedMemoryMB = usedMemoryMB;
            this.freeMemoryMB = freeMemoryMB;
            this.weakCacheSize = weakCacheSize;
        }
        
        public double getUsagePercent() {
            return (double) usedMemoryMB / maxMemoryMB * 100;
        }
    }
}
