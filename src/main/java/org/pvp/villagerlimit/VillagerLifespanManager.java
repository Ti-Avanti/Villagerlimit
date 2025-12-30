package org.pvp.villagerlimit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.pvp.villagerlimit.display.ArmorStandAdapter;
import org.pvp.villagerlimit.display.DisplayAdapter;
import org.pvp.villagerlimit.display.TextDisplayAdapter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 村民寿命管理器
 * 管理治愈获得的村民的寿命
 */
public class VillagerLifespanManager {
    
    private final Villagerlimit plugin;
    private final NamespacedKey lifespanKey;
    private final NamespacedKey displayKey;
    private final Map<UUID, Entity> displayCache;
    private final DisplayAdapter displayAdapter;
    
    public VillagerLifespanManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.lifespanKey = new NamespacedKey(plugin, "lifespan_end");
        this.displayKey = new NamespacedKey(plugin, "display_entity");
        this.displayCache = new ConcurrentHashMap<>();
        
        // 根据MC版本选择适配器
        this.displayAdapter = detectDisplayAdapter();
        
        plugin.getLogger().info("使用显示适配器: " + displayAdapter.getClass().getSimpleName());
        
        startLifespanCheckTask();
        startDisplayUpdateTask();
    }
    
    /**
     * 检测并选择合适的显示适配器
     */
    private DisplayAdapter detectDisplayAdapter() {
        try {
            // 尝试加载TextDisplay类
            Class.forName("org.bukkit.entity.TextDisplay");
            return new TextDisplayAdapter();
        } catch (ClassNotFoundException e) {
            // TextDisplay不存在，使用ArmorStand
            return new ArmorStandAdapter();
        }
    }
    
    /**
     * 设置村民寿命
     */
    public void setVillagerLifespan(Villager villager, int days) {
        long endTime = System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000);
        villager.getPersistentDataContainer().set(lifespanKey, PersistentDataType.LONG, endTime);
        
        // 创建显示实体
        createLifespanDisplay(villager);
    }
    
    /**
     * 获取村民剩余寿命（毫秒）
     */
    public long getRemainingLifespan(Villager villager) {
        Long endTime = villager.getPersistentDataContainer().get(lifespanKey, PersistentDataType.LONG);
        if (endTime == null) {
            return -1;
        }
        
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }
    
    /**
     * 检查村民是否有寿命限制
     */
    public boolean hasLifespan(Villager villager) {
        return villager.getPersistentDataContainer().has(lifespanKey, PersistentDataType.LONG);
    }
    
    /**
     * 创建寿命显示实体
     */
    private void createLifespanDisplay(Villager villager) {
        // 移除旧的显示实体
        removeLifespanDisplay(villager);
        
        // 创建新的显示实体
        String text = formatRemainingTime(getRemainingLifespan(villager));
        Entity display = displayAdapter.createDisplay(villager, text);
        
        // 保存显示实体UUID
        villager.getPersistentDataContainer().set(displayKey, PersistentDataType.STRING, display.getUniqueId().toString());
        displayCache.put(villager.getUniqueId(), display);
    }
    
    /**
     * 更新显示文本
     */
    private void updateDisplayText(Entity display, Villager villager) {
        long remaining = getRemainingLifespan(villager);
        if (remaining <= 0) {
            return;
        }
        
        String text = formatRemainingTime(remaining);
        displayAdapter.updateDisplay(display, text);
    }
    
    /**
     * 格式化剩余时间
     */
    private String formatRemainingTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        if (days > 0) {
            hours = hours % 24;
            return config.getLifespanDisplayFormat()
                .replace("{days}", String.valueOf(days))
                .replace("{hours}", String.valueOf(hours));
        } else if (hours > 0) {
            minutes = minutes % 60;
            return config.getLifespanDisplayFormat()
                .replace("{days}", "0")
                .replace("{hours}", String.valueOf(hours));
        } else if (minutes > 0) {
            return "§e剩余 " + minutes + " 分钟";
        } else {
            return "§c剩余 " + seconds + " 秒";
        }
    }
    
    /**
     * 移除寿命显示实体
     */
    private void removeLifespanDisplay(Villager villager) {
        // 从缓存移除
        Entity cached = displayCache.remove(villager.getUniqueId());
        if (cached != null) {
            displayAdapter.removeDisplay(cached);
        }
        
        // 从持久化数据获取
        String displayUUID = villager.getPersistentDataContainer().get(displayKey, PersistentDataType.STRING);
        if (displayUUID != null) {
            try {
                UUID uuid = UUID.fromString(displayUUID);
                var entity = Bukkit.getEntity(uuid);
                if (entity != null) {
                    displayAdapter.removeDisplay(entity);
                }
            } catch (Exception ignored) {
            }
            villager.getPersistentDataContainer().remove(displayKey);
        }
    }
    
    /**
     * 启动寿命检查任务
     */
    private void startLifespanCheckTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (var world : plugin.getServer().getWorlds()) {
                for (var entity : world.getEntities()) {
                    if (entity instanceof Villager villager) {
                        if (hasLifespan(villager)) {
                            long remaining = getRemainingLifespan(villager);
                            
                            if (remaining <= 0) {
                                // 寿命到期，移除村民
                                removeLifespanDisplay(villager);
                                villager.remove();
                                
                                VillagerLimitConfig config = plugin.getLimitConfig();
                                if (config.isLifespanNotifyEnabled()) {
                                    // 通知玩家
                                    String message = config.getLifespanExpiredMessage();
                                    int range = config.getLifespanNotifyRange();
                                    
                                    if (range <= 0) {
                                        // 全服通知
                                        plugin.getServer().getOnlinePlayers()
                                            .forEach(player -> player.sendMessage(message));
                                    } else {
                                        // 范围通知
                                        villager.getWorld().getNearbyPlayers(villager.getLocation(), range)
                                            .forEach(player -> player.sendMessage(message));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 20L * 60, 20L * 60); // 每分钟检查一次
    }
    
    /**
     * 启动显示更新任务
     */
    private void startDisplayUpdateTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (var world : plugin.getServer().getWorlds()) {
                for (var entity : world.getEntities()) {
                    if (entity instanceof Villager villager) {
                        if (hasLifespan(villager)) {
                            updateVillagerDisplay(villager);
                        }
                    }
                }
            }
        }, 20L * 10, 20L * 10); // 每10秒更新一次
    }
    
    /**
     * 更新村民显示
     */
    private void updateVillagerDisplay(Villager villager) {
        Entity display = displayCache.get(villager.getUniqueId());
        
        if (display == null || !displayAdapter.isValid(display)) {
            // 重新创建显示实体
            String displayUUID = villager.getPersistentDataContainer().get(displayKey, PersistentDataType.STRING);
            if (displayUUID != null) {
                try {
                    UUID uuid = UUID.fromString(displayUUID);
                    var entity = Bukkit.getEntity(uuid);
                    if (entity != null && displayAdapter.isValid(entity)) {
                        display = entity;
                        displayCache.put(villager.getUniqueId(), display);
                    } else {
                        createLifespanDisplay(villager);
                        return;
                    }
                } catch (Exception e) {
                    createLifespanDisplay(villager);
                    return;
                }
            } else {
                createLifespanDisplay(villager);
                return;
            }
        }
        
        // 更新位置和文本
        displayAdapter.updateLocation(display, villager.getLocation().add(0, villager.getHeight() + 0.5, 0));
        updateDisplayText(display, villager);
    }
    
    /**
     * 清理数据
     */
    public void cleanup() {
        for (Entity display : displayCache.values()) {
            displayAdapter.removeDisplay(display);
        }
        displayCache.clear();
    }
}
