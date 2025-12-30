package org.pvp.villagerlimit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

public class VillagerTradeListener implements Listener {
    
    private final Villagerlimit plugin;
    private final TradeDataManager tradeDataManager;
    private final TradeCooldownManager cooldownManager;
    private final TradeLimitManager limitManager;
    private final TradeStatisticsManager statisticsManager;
    private final EconomyBalanceManager economyManager;
    
    public VillagerTradeListener(Villagerlimit plugin) {
        this.plugin = plugin;
        this.tradeDataManager = new TradeDataManager(plugin);
        this.cooldownManager = new TradeCooldownManager(plugin);
        this.limitManager = new TradeLimitManager(plugin);
        this.statisticsManager = new TradeStatisticsManager(plugin);
        this.economyManager = new EconomyBalanceManager(plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory)) {
            return;
        }
        
        MerchantInventory merchantInventory = (MerchantInventory) event.getInventory();
        if (!(merchantInventory.getHolder() instanceof Villager)) {
            return;
        }
        
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Villager villager = (Villager) merchantInventory.getHolder();
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        // 如果完全禁用交易
        if (config.isDisableTrading()) {
            event.setCancelled(true);
            player.sendMessage("§c村民交易已被禁用！");
            return;
        }
        
        // 获取交易的物品
        MerchantRecipe recipe = merchantInventory.getSelectedRecipe();
        if (recipe == null) {
            return;
        }
        
        ItemStack result = recipe.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }
        
        String profession = villager.getProfession().name();
        
        // 检查交易冷却
        if (config.isCooldownEnabled() && cooldownManager.isOnCooldown(player, profession, result)) {
            event.setCancelled(true);
            long remaining = cooldownManager.getRemainingCooldown(player, profession, result);
            String message = config.getCooldownMessage()
                .replace("{time}", cooldownManager.formatTime(remaining));
            player.sendMessage(message);
            return;
        }
        
        // 检查交易次数限制
        if (config.isLimitEnabled() && !limitManager.canTrade(player, profession, result)) {
            event.setCancelled(true);
            player.sendMessage(config.getLimitReachedMessage());
            return;
        }
        
        // 检查绿宝石消耗
        if (config.isEconomyBalanceEnabled() && !economyManager.hasEnoughEmeralds(player, result)) {
            event.setCancelled(true);
            int required = economyManager.getRequiredEmeralds(result);
            String message = config.getInsufficientEmeraldMessage()
                .replace("{required}", String.valueOf(required));
            player.sendMessage(message);
            return;
        }
        
        // 检查经验消耗
        int requiredExp = 0;
        if (config.isExpCostEnabled()) {
            requiredExp = calculateExpCost(player, villager, result);
            String costMode = config.getExpCostMode();
            
            if ("POINTS".equalsIgnoreCase(costMode)) {
                // 经验值模式
                int currentExp = getTotalExperience(player);
                
                if (currentExp < requiredExp) {
                    event.setCancelled(true);
                    String message = config.getInsufficientPointsMessage()
                        .replace("{required}", String.valueOf(requiredExp))
                        .replace("{current}", String.valueOf(currentExp));
                    player.sendMessage(message);
                    return;
                }
                
                // 检查扣除后是否会低于最低等级
                int minLevel = config.getMinLevel();
                if (minLevel > 0) {
                    int remainingExp = currentExp - requiredExp;
                    int minLevelExp = 0;
                    for (int i = 0; i < minLevel; i++) {
                        minLevelExp += getExperienceToLevelUp(i);
                    }
                    
                    if (remainingExp < minLevelExp) {
                        event.setCancelled(true);
                        player.sendMessage("§c交易会使你的等级低于最低保留等级！");
                        return;
                    }
                }
                
                // 扣除经验值
                setTotalExperience(player, currentExp - requiredExp);
            } else {
                // 等级模式（默认）
                int currentLevel = player.getLevel();
                
                if (currentLevel < requiredExp) {
                    event.setCancelled(true);
                    String message = config.getInsufficientExpMessage()
                        .replace("{required}", String.valueOf(requiredExp))
                        .replace("{current}", String.valueOf(currentLevel));
                    player.sendMessage(message);
                    return;
                }
                
                // 扣除等级
                int minLevel = config.getMinLevel();
                if (currentLevel - requiredExp >= minLevel) {
                    player.setLevel(currentLevel - requiredExp);
                } else {
                    event.setCancelled(true);
                    player.sendMessage("§c交易会使你的等级低于最低保留等级！");
                    return;
                }
            }
        }
        
        // 消耗绿宝石
        economyManager.consumeEmeralds(player, result);
        
        // 检查成本递增
        if (config.isCostScalingEnabled()) {
            double multiplier = tradeDataManager.getTradeMultiplier(player, result);
            if (multiplier > 1.0) {
                String message = config.getScalingMessage()
                    .replace("{cost}", String.format("%.1f", multiplier));
                player.sendMessage(message);
            }
            
            // 记录交易
            tradeDataManager.recordTrade(player, result);
        }
        
        // 设置冷却
        if (config.isCooldownEnabled()) {
            cooldownManager.setCooldown(player, profession, result);
        }
        
        // 记录次数限制
        if (config.isLimitEnabled()) {
            limitManager.recordTrade(player, profession, result);
        }
        
        // 记录统计
        if (config.isStatisticsEnabled()) {
            statisticsManager.recordTrade(player, result, requiredExp);
        }
    }
    
    private int calculateExpCost(Player player, Villager villager, ItemStack result) {
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        // 基础消耗
        int baseCost = config.getBaseExpCost();
        
        // 职业消耗
        String profession = villager.getProfession().name();
        int professionCost = config.getProfessionExpCost(profession);
        
        // 物品消耗
        String material = result.getType().name();
        int itemCost = config.getValuableItemExpCost(material);
        
        // 取最大值
        int totalCost = Math.max(baseCost, professionCost);
        if (itemCost > 0) {
            totalCost = Math.max(totalCost, itemCost);
        }
        
        // 应用成本递增
        if (config.isCostScalingEnabled()) {
            double multiplier = tradeDataManager.getTradeMultiplier(player, result);
            totalCost = (int) Math.ceil(totalCost * multiplier);
        }
        
        // 应用权限组倍率
        double permMultiplier = getPermissionMultiplier(player);
        totalCost = (int) Math.ceil(totalCost * permMultiplier);
        
        return totalCost;
    }
    
    private double getPermissionMultiplier(Player player) {
        PermissionGroupManager.PermissionGroup group = plugin.getPermissionGroupManager().getPlayerGroup(player);
        return group.expCostMultiplier;
    }
    
    /**
     * 获取玩家总经验值
     */
    private int getTotalExperience(Player player) {
        int exp = 0;
        int level = player.getLevel();
        
        // 累加之前所有等级的经验
        for (int i = 0; i < level; i++) {
            exp += getExperienceToLevelUp(i);
        }
        
        // 加上当前等级的进度经验
        exp += Math.round(getExperienceToLevelUp(level) * player.getExp());
        
        return exp;
    }
    
    /**
     * 设置玩家总经验值
     */
    private void setTotalExperience(Player player, int exp) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        
        int level = 0;
        int expToLevel = getExperienceToLevelUp(level);
        
        while (exp >= expToLevel) {
            exp -= expToLevel;
            level++;
            expToLevel = getExperienceToLevelUp(level);
        }
        
        player.setLevel(level);
        player.setExp((float) exp / (float) expToLevel);
    }
    
    /**
     * 获取升到下一级所需经验
     */
    private int getExperienceToLevelUp(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }
    
    public TradeDataManager getTradeDataManager() {
        return tradeDataManager;
    }
    
    public TradeStatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
    
    public TradeLimitManager getLimitManager() {
        return limitManager;
    }
}
