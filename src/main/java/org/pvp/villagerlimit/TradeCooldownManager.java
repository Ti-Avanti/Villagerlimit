package org.pvp.villagerlimit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TradeCooldownManager {
    
    private final Villagerlimit plugin;
    private final Map<String, Long> cooldowns;
    
    public TradeCooldownManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
    }
    
    public boolean isOnCooldown(Player player, String profession, ItemStack item) {
        String key = generateKey(player, profession, item);
        Long cooldownEnd = cooldowns.get(key);
        
        if (cooldownEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() >= cooldownEnd) {
            cooldowns.remove(key);
            return false;
        }
        
        return true;
    }
    
    public long getRemainingCooldown(Player player, String profession, ItemStack item) {
        String key = generateKey(player, profession, item);
        Long cooldownEnd = cooldowns.get(key);
        
        if (cooldownEnd == null) {
            return 0;
        }
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }
    
    public void setCooldown(Player player, String profession, ItemStack item) {
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        int cooldownSeconds = config.getDefaultCooldown();
        
        // 检查职业冷却
        int professionCooldown = config.getProfessionCooldown(profession);
        if (professionCooldown > 0) {
            cooldownSeconds = professionCooldown;
        }
        
        // 检查物品冷却
        int itemCooldown = config.getItemCooldown(item.getType().name());
        if (itemCooldown > 0) {
            cooldownSeconds = Math.max(cooldownSeconds, itemCooldown);
        }
        
        // 应用权限组倍率
        double multiplier = getPermissionMultiplier(player, "cooldown");
        cooldownSeconds = (int) (cooldownSeconds * multiplier);
        
        String key = generateKey(player, profession, item);
        cooldowns.put(key, System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }
    
    public String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    private String generateKey(Player player, String profession, ItemStack item) {
        return player.getUniqueId().toString() + ":" + profession + ":" + item.getType().name();
    }
    
    private double getPermissionMultiplier(Player player, String type) {
        PermissionGroupManager.PermissionGroup group = plugin.getPermissionGroupManager().getPlayerGroup(player);
        if (type.equals("cooldown")) {
            return group.cooldownMultiplier;
        }
        return 1.0;
    }
    
    public void clearData() {
        cooldowns.clear();
    }
    
    /**
     * 重载配置时清空冷却缓存
     * 这样可以让新的冷却时间配置立即生效
     */
    public void reload() {
        cooldowns.clear();
        plugin.getLogger().info("交易冷却数据已清空");
    }
}
