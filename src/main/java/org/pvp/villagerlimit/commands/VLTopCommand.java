package org.pvp.villagerlimit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pvp.villagerlimit.TradeStatisticsManager;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VLTopCommand implements CommandExecutor {
    
    private final Villagerlimit plugin;
    private final TradeStatisticsManager statsManager;
    
    public VLTopCommand(Villagerlimit plugin, TradeStatisticsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.top")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        List<Map.Entry<UUID, Integer>> leaderboard = statsManager.getLeaderboard();
        
        if (leaderboard.isEmpty()) {
            sender.sendMessage("§e暂无交易排行数据");
            return true;
        }
        
        sender.sendMessage("§6========== §e交易排行榜 §6==========");
        
        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : leaderboard) {
            TradeStatisticsManager.PlayerTradeStats stats = statsManager.getPlayerStats(entry.getKey());
            if (stats != null) {
                String rankColor = getRankColor(rank);
                sender.sendMessage(rankColor + "#" + rank + " §6" + stats.playerName + 
                    " §7- §e" + entry.getValue() + " §7次交易");
                rank++;
            }
        }
        
        return true;
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
