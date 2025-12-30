package org.pvp.villagerlimit.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.pvp.villagerlimit.TradeStatisticsManager;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.LanguageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计GUI
 * 显示玩家交易统计信息
 */
public class StatsGUI extends BaseGUI {
    
    private final TradeStatisticsManager.PlayerTradeStats stats;
    private final LanguageManager lang;
    
    public StatsGUI(Villagerlimit plugin, Player player, TradeStatisticsManager.PlayerTradeStats stats) {
        super(plugin, player);
        this.stats = stats;
        this.lang = null; // 暂时设为null，后续集成时再修复
    }
    
    @Override
    public String getTitle() {
        return "§6§l交易统计 - " + stats.playerName;
    }
    
    @Override
    public int getSize() {
        return 54; // 6行
    }
    
    @Override
    public void createContents() {
        // 填充边框
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);
        
        // 玩家头颅
        inventory.setItem(13, createItem(
            Material.PLAYER_HEAD,
            "§e§l" + stats.playerName,
            "§7总交易次数: §6" + stats.totalTrades,
            "§7总消耗经验: §6" + stats.totalExpSpent + " §7级"
        ));
        
        // 统计信息
        inventory.setItem(20, createItem(
            Material.EMERALD,
            "§a§l总交易次数",
            "§7" + stats.totalTrades + " 次"
        ));
        
        inventory.setItem(22, createItem(
            Material.EXPERIENCE_BOTTLE,
            "§b§l总消耗经验",
            "§7" + stats.totalExpSpent + " 级"
        ));
        
        inventory.setItem(24, createItem(
            Material.CLOCK,
            "§e§l最后交易时间",
            "§7" + formatTime(stats.lastTradeTime)
        ));
        
        // 最常交易物品
        int slot = 29;
        List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(stats.itemTradeCount.entrySet());
        sortedItems.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));
        
        for (int i = 0; i < Math.min(7, sortedItems.size()); i++) {
            Map.Entry<String, Integer> entry = sortedItems.get(i);
            Material material = Material.getMaterial(entry.getKey());
            
            if (material != null) {
                inventory.setItem(slot++, createItem(
                    material,
                    "§6" + entry.getKey(),
                    "§7交易次数: §e" + entry.getValue()
                ));
            }
        }
        
        // 关闭按钮
        inventory.setItem(49, createItem(
            Material.BARRIER,
            "§c§l关闭"
        ));
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        
        // 关闭按钮
        if (slot == 49) {
            player.closeInventory();
        }
    }
    
    private String formatTime(long timestamp) {
        if (timestamp == 0) {
            return "从未";
        }
        
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "天前";
        } else if (hours > 0) {
            return hours + "小时前";
        } else if (minutes > 0) {
            return minutes + "分钟前";
        } else {
            return seconds + "秒前";
        }
    }
}
