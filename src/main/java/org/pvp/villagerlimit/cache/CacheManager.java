package org.pvp.villagerlimit.cache;

import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器
 * 提供多级缓存支持，减少数据库查询
 */
public class CacheManager extends AbstractModule {
    
    // 玩家数据缓存
    private final Map<UUID, PlayerDataCache> playerDataCache;
    
    // 权限组缓存
    private final Map<UUID, String> permissionGroupCache;
    
    // 配置缓存
    private volatile ConfigCache configCache;
    
    // 缓存过期时间（毫秒）
    private long cacheExpireTime;
    
    public CacheManager(Villagerlimit plugin) {
        super(plugin);
        this.playerDataCache = new ConcurrentHashMap<>();
        this.permissionGroupCache = new ConcurrentHashMap<>();
    }
    
    @Override
    public String getName() {
        return "CacheManager";
    }
    
    @Override
    public void onEnable() {
        // 从配置读取缓存过期时间（默认5分钟）
        this.cacheExpireTime = plugin.getConfig().getLong("cache.expire-time", 300) * 1000;
        
        // 初始化配置缓存
        reloadConfigCache();
        
        // 启动定时清理任务
        startCleanupTask();
        
        info("缓存系统已启用，过期时间: " + (cacheExpireTime / 1000) + "秒");
    }
    
    @Override
    public void onDisable() {
        playerDataCache.clear();
        permissionGroupCache.clear();
        info("缓存已清空");
    }
    
    @Override
    public void onReload() {
        reloadConfigCache();
        info("配置缓存已重载");
    }
    
    /**
     * 获取玩家数据缓存
     */
    public PlayerDataCache getPlayerData(UUID uuid) {
        PlayerDataCache cache = playerDataCache.get(uuid);
        if (cache != null && !cache.isExpired()) {
            return cache;
        }
        return null;
    }
    
    /**
     * 设置玩家数据缓存
     */
    public void setPlayerData(UUID uuid, PlayerDataCache data) {
        data.setExpireTime(System.currentTimeMillis() + cacheExpireTime);
        playerDataCache.put(uuid, data);
    }
    
    /**
     * 移除玩家数据缓存
     */
    public void removePlayerData(UUID uuid) {
        playerDataCache.remove(uuid);
    }
    
    /**
     * 获取权限组缓存
     */
    public String getPermissionGroup(UUID uuid) {
        return permissionGroupCache.get(uuid);
    }
    
    /**
     * 设置权限组缓存
     */
    public void setPermissionGroup(UUID uuid, String group) {
        permissionGroupCache.put(uuid, group);
    }
    
    /**
     * 获取配置缓存
     */
    public ConfigCache getConfigCache() {
        return configCache;
    }
    
    /**
     * 重载配置缓存
     */
    private void reloadConfigCache() {
        configCache = new ConfigCache(plugin);
    }
    
    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            
            // 清理过期的玩家数据
            playerDataCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            
            // 清理离线玩家的权限组缓存
            permissionGroupCache.keySet().removeIf(uuid -> 
                plugin.getServer().getPlayer(uuid) == null
            );
            
        }, 20L * 60, 20L * 60); // 每分钟执行一次
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAll() {
        playerDataCache.clear();
        permissionGroupCache.clear();
        reloadConfigCache();
        info("所有缓存已清空");
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return new CacheStats(
            playerDataCache.size(),
            permissionGroupCache.size(),
            cacheExpireTime
        );
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        public final int playerDataCount;
        public final int permissionGroupCount;
        public final long expireTime;
        
        public CacheStats(int playerDataCount, int permissionGroupCount, long expireTime) {
            this.playerDataCount = playerDataCount;
            this.permissionGroupCount = permissionGroupCount;
            this.expireTime = expireTime;
        }
    }
}
