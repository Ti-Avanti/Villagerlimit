package org.pvp.villagerlimit.cache;

import org.bukkit.configuration.ConfigurationSection;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置缓存
 * 缓存常用配置项，避免重复读取
 */
public class ConfigCache {
    
    // 交易控制配置
    public final boolean expCostEnabled;
    public final int baseExpCost;
    public final Map<String, Integer> professionExpCost;
    public final Map<String, Integer> valuableItemsExpCost;
    
    public final boolean costScalingEnabled;
    public final double scalingMultiplier;
    public final double maxMultiplier;
    
    public final boolean cooldownEnabled;
    public final int defaultCooldown;
    public final Map<String, Integer> professionCooldown;
    public final Map<String, Integer> itemCooldown;
    
    public final boolean limitEnabled;
    public final int globalLimit;
    public final Map<String, Integer> professionLimit;
    public final Map<String, Integer> itemLimit;
    
    // 村民限制配置
    public final boolean villagerLimitEnabled;
    public final int chunkRadius;
    public final int maxVillagers;
    
    // AI优化配置
    public final boolean aiOptimizationEnabled;
    
    public ConfigCache(Villagerlimit plugin) {
        ConfigurationSection config = plugin.getConfig();
        
        // 交易经验消耗
        ConfigurationSection expCost = config.getConfigurationSection("trade-control.exp-cost");
        this.expCostEnabled = expCost.getBoolean("enabled", true);
        this.baseExpCost = expCost.getInt("base-cost", 5);
        this.professionExpCost = loadIntMap(expCost.getConfigurationSection("per-profession"));
        this.valuableItemsExpCost = loadIntMap(expCost.getConfigurationSection("valuable-items"));
        
        // 成本递增
        ConfigurationSection scaling = config.getConfigurationSection("trade-control.cost-scaling");
        this.costScalingEnabled = scaling.getBoolean("enabled", true);
        this.scalingMultiplier = scaling.getDouble("multiplier", 1.5);
        this.maxMultiplier = scaling.getDouble("max-multiplier", 10.0);
        
        // 交易冷却
        ConfigurationSection cooldown = config.getConfigurationSection("trade-control.cooldown");
        this.cooldownEnabled = cooldown.getBoolean("enabled", true);
        this.defaultCooldown = cooldown.getInt("default-cooldown", 300);
        this.professionCooldown = loadIntMap(cooldown.getConfigurationSection("per-profession"));
        this.itemCooldown = loadIntMap(cooldown.getConfigurationSection("per-item"));
        
        // 交易限制
        ConfigurationSection limit = config.getConfigurationSection("trade-control.limit");
        this.limitEnabled = limit.getBoolean("enabled", true);
        this.globalLimit = limit.getInt("global-limit", 50);
        this.professionLimit = loadIntMap(limit.getConfigurationSection("per-profession"));
        this.itemLimit = loadIntMap(limit.getConfigurationSection("per-item"));
        
        // 村民限制
        ConfigurationSection villagerLimit = config.getConfigurationSection("villager-limit");
        this.villagerLimitEnabled = villagerLimit.getBoolean("enabled", true);
        this.chunkRadius = villagerLimit.getInt("chunk-radius", 3);
        this.maxVillagers = villagerLimit.getInt("max-villagers", 5);
        
        // AI优化
        this.aiOptimizationEnabled = config.getBoolean("ai-optimization.enabled", true);
    }
    
    private Map<String, Integer> loadIntMap(ConfigurationSection section) {
        Map<String, Integer> map = new HashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                map.put(key, section.getInt(key));
            }
        }
        return map;
    }
}
