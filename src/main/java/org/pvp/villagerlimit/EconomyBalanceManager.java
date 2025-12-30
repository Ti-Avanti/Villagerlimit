package org.pvp.villagerlimit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EconomyBalanceManager {
    
    private final Villagerlimit plugin;
    
    public EconomyBalanceManager(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    public boolean hasEnoughEmeralds(Player player, ItemStack item) {
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        if (!config.isEconomyBalanceEnabled() || !config.isRequireExtraEmeralds()) {
            return true;
        }
        
        int requiredEmeralds = getRequiredEmeralds(item);
        if (requiredEmeralds <= 0) {
            return true;
        }
        
        int playerEmeralds = countEmeralds(player);
        return playerEmeralds >= requiredEmeralds;
    }
    
    public void consumeEmeralds(Player player, ItemStack item) {
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        if (!config.isEconomyBalanceEnabled() || !config.isRequireExtraEmeralds()) {
            return;
        }
        
        int requiredEmeralds = getRequiredEmeralds(item);
        if (requiredEmeralds <= 0) {
            return;
        }
        
        removeEmeralds(player, requiredEmeralds);
    }
    
    public int getRequiredEmeralds(ItemStack item) {
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        int baseEmeralds = config.getValuableItemEmeraldCost(item.getType().name());
        if (baseEmeralds <= 0) {
            return 0;
        }
        
        double multiplier = config.getEmeraldCostMultiplier();
        return (int) Math.ceil(baseEmeralds * multiplier);
    }
    
    private int countEmeralds(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private void removeEmeralds(Player player, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    item.setAmount(0);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
                
                if (remaining == 0) {
                    break;
                }
            }
        }
    }
}
