package org.pvp.villagerlimit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TradeLimitManager {
    
    private final Villagerlimit plugin;
    private final Map<String, LimitRecord> limitRecords;
    private LocalDate lastReset;
    
    public TradeLimitManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.limitRecords = new ConcurrentHashMap<>();
        this.lastReset = LocalDate.now();
    }
    
    public boolean canTrade(Player player, String profession, ItemStack item) {
        checkAndReset();
        
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        // 检查全局限制
        int globalLimit = config.getGlobalTradeLimit() + getPermissionBonus(player);
        int globalCount = getGlobalTradeCount(player);
        if (globalCount >= globalLimit) {
            return false;
        }
        
        // 检查职业限制
        int professionLimit = config.getProfessionTradeLimit(profession);
        if (professionLimit > 0) {
            int professionCount = getProfessionTradeCount(player, profession);
            if (professionCount >= professionLimit) {
                return false;
            }
        }
        
        // 检查物品限制
        int itemLimit = config.getItemTradeLimit(item.getType().name());
        if (itemLimit > 0) {
            int itemCount = getItemTradeCount(player, item);
            if (itemCount >= itemLimit) {
                return false;
            }
        }
        
        return true;
    }
    
    public void recordTrade(Player player, String profession, ItemStack item) {
        checkAndReset();
        
        String globalKey = player.getUniqueId().toString() + ":global";
        String professionKey = player.getUniqueId().toString() + ":profession:" + profession;
        String itemKey = player.getUniqueId().toString() + ":item:" + item.getType().name();
        
        limitRecords.computeIfAbsent(globalKey, k -> new LimitRecord()).count++;
        limitRecords.computeIfAbsent(professionKey, k -> new LimitRecord()).count++;
        limitRecords.computeIfAbsent(itemKey, k -> new LimitRecord()).count++;
    }
    
    public int getGlobalTradeCount(Player player) {
        String key = player.getUniqueId().toString() + ":global";
        LimitRecord record = limitRecords.get(key);
        return record != null ? record.count : 0;
    }
    
    public int getProfessionTradeCount(Player player, String profession) {
        String key = player.getUniqueId().toString() + ":profession:" + profession;
        LimitRecord record = limitRecords.get(key);
        return record != null ? record.count : 0;
    }
    
    public int getItemTradeCount(Player player, ItemStack item) {
        String key = player.getUniqueId().toString() + ":item:" + item.getType().name();
        LimitRecord record = limitRecords.get(key);
        return record != null ? record.count : 0;
    }
    
    private void checkAndReset() {
        VillagerLimitConfig config = plugin.getLimitConfig();
        String resetPeriod = config.getResetPeriod();
        LocalDate now = LocalDate.now();
        
        boolean shouldReset = false;
        
        switch (resetPeriod.toUpperCase()) {
            case "DAILY":
                shouldReset = !now.equals(lastReset);
                break;
            case "WEEKLY":
                shouldReset = ChronoUnit.WEEKS.between(lastReset, now) >= 1;
                break;
            case "MONTHLY":
                shouldReset = ChronoUnit.MONTHS.between(lastReset, now) >= 1;
                break;
        }
        
        if (shouldReset) {
            limitRecords.clear();
            lastReset = now;
            plugin.getLogger().info("交易次数限制已重置");
        }
    }
    
    private int getPermissionBonus(Player player) {
        PermissionGroupManager.PermissionGroup group = plugin.getPermissionGroupManager().getPlayerGroup(player);
        return group.dailyLimitBonus;
    }
    
    public void clearData() {
        limitRecords.clear();
    }
    
    private static class LimitRecord {
        int count = 0;
    }
}
