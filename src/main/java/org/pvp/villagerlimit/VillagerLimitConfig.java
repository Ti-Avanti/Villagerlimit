package org.pvp.villagerlimit;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class VillagerLimitConfig {
    
    private final Villagerlimit plugin;
    private FileConfiguration config;
    
    public VillagerLimitConfig(Villagerlimit plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    // 村民数量限制设置
    public boolean isVillagerLimitEnabled() {
        return config.getBoolean("villager-limit.enabled", true);
    }
    
    public int getChunkRadius() {
        return config.getInt("villager-limit.chunk-radius", 3);
    }
    
    public int getMaxVillagers() {
        return config.getInt("villager-limit.max-villagers", 5);
    }
    
    // 生成控制设置
    public boolean isBlockNaturalSpawn() {
        return config.getBoolean("spawn-control.block-natural-spawn", true);
    }
    
    public boolean isAllowCure() {
        return config.getBoolean("spawn-control.allow-cure", true);
    }
    
    // 刷怪蛋设置
    public boolean isAllowSpawnEgg() {
        return config.getBoolean("spawn-control.allow-spawn-egg", true);
    }
    
    // 繁殖设置
    public boolean isBreedingAllowed() {
        return config.getBoolean("spawn-control.allow-breeding", true);
    }
    
    // AI优化设置
    public boolean isAIOptimizationEnabled() {
        return config.getBoolean("ai-optimization.enabled", true);
    }
    
    public List<String> getDisabledGoals() {
        return config.getStringList("ai-optimization.disable-goals");
    }
    
    // 交易设置
    public boolean isDisableTrading() {
        return config.getBoolean("trade-control.disable-trading", false);
    }
    
    // 经验消耗设置
    public boolean isExpCostEnabled() {
        return config.getBoolean("trade-control.exp-cost.enabled", true);
    }
    
    public String getExpCostMode() {
        return config.getString("trade-control.exp-cost.cost-mode", "LEVEL");
    }
    
    public int getBaseExpCost() {
        return config.getInt("trade-control.exp-cost.base-cost", 5);
    }
    
    public int getProfessionExpCost(String profession) {
        return config.getInt("trade-control.exp-cost.per-profession." + profession, getBaseExpCost());
    }
    
    public int getValuableItemExpCost(String material) {
        return config.getInt("trade-control.exp-cost.valuable-items." + material, 0);
    }
    
    public int getMinLevel() {
        return config.getInt("trade-control.exp-cost.min-level", 0);
    }
    
    // 成本递增设置
    public boolean isCostScalingEnabled() {
        return config.getBoolean("trade-control.cost-scaling.enabled", true);
    }
    
    public String getScalingType() {
        return config.getString("trade-control.cost-scaling.scaling-type", "MULTIPLIER");
    }
    
    public double getMultiplier() {
        return config.getDouble("trade-control.cost-scaling.multiplier", 1.5);
    }
    
    public double getMaxMultiplier() {
        return config.getDouble("trade-control.cost-scaling.max-multiplier", 10.0);
    }
    
    public int getAdditiveAmount() {
        return config.getInt("trade-control.cost-scaling.additive-amount", 1);
    }
    
    public int getResetHours() {
        return config.getInt("trade-control.cost-scaling.reset-hours", 24);
    }
    
    public double getDecayRate() {
        return config.getDouble("trade-control.cost-scaling.decay-rate", 0.0);
    }
    
    // 命令设置
    public boolean isKillVillagersEnabled() {
        return config.getBoolean("commands.killvillagers-enabled", true);
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    // 调试模式
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }
    
    // 交易冷却设置
    public boolean isCooldownEnabled() {
        return config.getBoolean("trade-control.cooldown.enabled", true);
    }
    
    public int getDefaultCooldown() {
        return config.getInt("trade-control.cooldown.default-cooldown", 300);
    }
    
    public int getProfessionCooldown(String profession) {
        return config.getInt("trade-control.cooldown.per-profession." + profession, 0);
    }
    
    public int getItemCooldown(String material) {
        return config.getInt("trade-control.cooldown.per-item." + material, 0);
    }
    
    // 交易次数限制设置
    public boolean isLimitEnabled() {
        return config.getBoolean("trade-control.limit.enabled", true);
    }
    
    public String getResetPeriod() {
        return config.getString("trade-control.limit.reset-period", "DAILY");
    }
    
    public int getGlobalTradeLimit() {
        return config.getInt("trade-control.limit.global-limit", 50);
    }
    
    public int getProfessionTradeLimit(String profession) {
        return config.getInt("trade-control.limit.per-profession." + profession, 0);
    }
    
    public int getItemTradeLimit(String material) {
        return config.getInt("trade-control.limit.per-item." + material, 0);
    }
    
    // 权限组设置
    public double getPermissionGroupMultiplier(String group, String type) {
        return config.getDouble("trade-control.permission-groups." + group + "." + type + "-multiplier", 1.0);
    }
    
    public int getPermissionGroupBonus(String group) {
        return config.getInt("trade-control.permission-groups." + group + ".daily-limit-bonus", 0);
    }
    
    // 经济平衡设置
    public boolean isEconomyBalanceEnabled() {
        return config.getBoolean("trade-control.economy-balance.enabled", true);
    }
    
    public double getEmeraldCostMultiplier() {
        return config.getDouble("trade-control.economy-balance.emerald-cost-multiplier", 1.5);
    }
    
    public boolean isRequireExtraEmeralds() {
        return config.getBoolean("trade-control.economy-balance.require-extra-emeralds", true);
    }
    
    public int getValuableItemEmeraldCost(String material) {
        return config.getInt("trade-control.economy-balance.valuable-items-emerald-cost." + material, 0);
    }
    
    // 交易统计设置
    public boolean isStatisticsEnabled() {
        return config.getBoolean("trade-control.statistics.enabled", true);
    }
    
    public boolean isDetailedLoggingEnabled() {
        return config.getBoolean("trade-control.statistics.detailed-logging", true);
    }
    
    public boolean isLeaderboardEnabled() {
        return config.getBoolean("trade-control.statistics.leaderboard-enabled", true);
    }
    
    public int getLeaderboardUpdateInterval() {
        return config.getInt("trade-control.statistics.leaderboard-update-interval", 30);
    }
    
    // 村民寿命设置
    public boolean isLifespanEnabled() {
        return config.getBoolean("villager-lifespan.enabled", false);
    }
    
    public int getVillagerLifespanDays() {
        return config.getInt("villager-lifespan.days", 7);
    }
    
    // 寿命天数的简化别名
    public int getLifespanDays() {
        return getVillagerLifespanDays();
    }
    
    public boolean isLifespanNotifyEnabled() {
        return config.getBoolean("villager-lifespan.notify-enabled", true);
    }
    
    public int getLifespanNotifyRange() {
        return config.getInt("villager-lifespan.notify-range", 16);
    }
    
    // 自动添加寿命设置
    public boolean isAutoAddLifespanEnabled() {
        return config.getBoolean("villager-lifespan.auto-add-lifespan.enabled", true);
    }
    
    public int getAutoAddCheckInterval() {
        return config.getInt("villager-lifespan.auto-add-lifespan.check-interval", 300);
    }
    
    public boolean isAutoAddCheckOnStartup() {
        return config.getBoolean("villager-lifespan.auto-add-lifespan.check-on-startup", true);
    }
}
