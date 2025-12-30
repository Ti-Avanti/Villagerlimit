package org.pvp.villagerlimit.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.pvp.villagerlimit.TradeStatisticsManager;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 排行榜GUI
 */
public class LeaderboardGUI extends BaseGUI {
    
    private final TradeStatisticsManager statsManager;
    
    public LeaderboardGUI(Villagerlimit plugin, Player player, TradeStatisticsManager statsManager) {
        super(plugin, player);
        this.statsManager = statsManager;
    }
    
    @Override
    public String getTitle() {
        return "§6§l交易排行榜";
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void createContents() {
        fillBorder(Material.YELLOW_STAINED_GLASS_PANE);
        
        // 标题
        inventory.setItem(4, createItem(
            Material.GOLD_BLOCK,
            "§e§l交易排行榜",
            "§7前10名玩家"
        ));
        
        // 获取排行榜
        List<Map.Entry<UUID, Integer>> leaderboard = statsManager.getLeaderboard();
        
        if (leaderboard.isEmpty()) {
            inventory.setItem(22, createItem(
                Material.BARRIER,
                "§c暂无排行数据"
            ));
            return;
        }
        
        // 显示排行
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
            Map.Entry<UUID, Integer> entry = leaderboard.get(i);
            TradeStatisticsManager.PlayerTradeStats stats = statsManager.getPlayerStats(entry.getKey());
            
            if (stats != null) {
                Material material = getRankMaterial(i + 1);
                inventory.setItem(slots[i], createItem(
                    material,
                    getRankColor(i + 1) + "#" + (i + 1) + " §6" + stats.playerName,
                    "§7交易次数: §e" + entry.getValue(),
                    "§7消耗经验: §e" + stats.totalExpSpent + " §7级",
                    "",
                    "§e点击查看详情"
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
        
        if (slot == 49) {
            player.closeInventory();
        }
    }
    
    private Material getRankMaterial(int rank) {
        switch (rank) {
            case 1: return Material.GOLD_BLOCK;
            case 2: return Material.IRON_BLOCK;
            case 3: return Material.COPPER_BLOCK;
            default: return Material.PLAYER_HEAD;
        }
    }
    
    private String getRankColor(int rank) {
        switch (rank) {
            case 1: return "§e";
            case 2: return "§7";
            case 3: return "§c";
            default: return "§f";
        }
    }
}
