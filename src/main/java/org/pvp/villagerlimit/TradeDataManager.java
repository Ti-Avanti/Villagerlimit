package org.pvp.villagerlimit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TradeDataManager {
    
    private final Villagerlimit plugin;
    private final Map<String, TradeRecord> tradeRecords;
    
    public TradeDataManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.tradeRecords = new ConcurrentHashMap<>();
    }
    
    public double getTradeMultiplier(Player player, ItemStack item) {
        String key = generateKey(player, item);
        TradeRecord record = tradeRecords.get(key);
        
        if (record == null) {
            return 1.0;
        }
        
        // 检查是否需要重置
        VillagerLimitConfig config = plugin.getLimitConfig();
        int resetHours = config.getResetHours();
        if (resetHours > 0) {
            long hoursPassed = (System.currentTimeMillis() - record.lastTradeTime) / (1000 * 60 * 60);
            if (hoursPassed >= resetHours) {
                tradeRecords.remove(key);
                return 1.0;
            }
        }
        
        // 应用衰减
        double decayRate = config.getDecayRate();
        if (decayRate > 0) {
            long hoursPassed = (System.currentTimeMillis() - record.lastTradeTime) / (1000 * 60 * 60);
            double decay = 1.0 - (decayRate * hoursPassed);
            if (decay < 0) decay = 0;
            record.multiplier = 1.0 + ((record.multiplier - 1.0) * decay);
            if (record.multiplier < 1.0) record.multiplier = 1.0;
        }
        
        return record.multiplier;
    }
    
    public void recordTrade(Player player, ItemStack item) {
        String key = generateKey(player, item);
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        TradeRecord record = tradeRecords.computeIfAbsent(key, k -> new TradeRecord());
        
        String scalingType = config.getScalingType();
        double maxMultiplier = config.getMaxMultiplier();
        
        switch (scalingType.toUpperCase()) {
            case "MULTIPLIER":
                double multiplier = config.getMultiplier();
                record.multiplier *= multiplier;
                if (record.multiplier > maxMultiplier) {
                    record.multiplier = maxMultiplier;
                }
                break;
                
            case "ADDITIVE":
                int additive = config.getAdditiveAmount();
                record.multiplier += additive;
                if (record.multiplier > maxMultiplier) {
                    record.multiplier = maxMultiplier;
                }
                break;
                
            case "STEPPED":
                record.tradeCount++;
                if (record.tradeCount <= 5) {
                    record.multiplier = 1.0;
                } else if (record.tradeCount <= 10) {
                    record.multiplier = 2.0;
                } else if (record.tradeCount <= 20) {
                    record.multiplier = 5.0;
                } else {
                    record.multiplier = maxMultiplier;
                }
                break;
        }
        
        record.lastTradeTime = System.currentTimeMillis();
    }
    
    private String generateKey(Player player, ItemStack item) {
        return player.getUniqueId().toString() + ":" + item.getType().name();
    }
    
    public void clearData() {
        tradeRecords.clear();
    }
    
    private static class TradeRecord {
        double multiplier = 1.0;
        int tradeCount = 0;
        long lastTradeTime = System.currentTimeMillis();
    }
}
