package org.pvp.villagerlimit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.pvp.villagerlimit.TradeStatisticsManager;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.gui.GUIManager;
import org.pvp.villagerlimit.gui.LeaderboardGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VLTopCommand implements CommandExecutor, TabCompleter {
    
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
        
        // 如果是玩家执行命令，打开GUI
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GUIManager guiManager = plugin.getModuleManager().getModule(GUIManager.class);
            if (guiManager != null) {
                LeaderboardGUI gui = new LeaderboardGUI(plugin, player, statsManager);
                guiManager.openGUI(player, gui);
                return true;
            }
        }
        
        // 控制台或GUI不可用时显示文本
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // vltop 命令没有参数
        return new ArrayList<>();
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
