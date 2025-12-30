package org.pvp.villagerlimit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.pvp.villagerlimit.TradeStatisticsManager;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.UUID;

public class VLStatsCommand implements CommandExecutor {
    
    private final Villagerlimit plugin;
    private final TradeStatisticsManager statsManager;
    
    public VLStatsCommand(Villagerlimit plugin, TradeStatisticsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.stats")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        Player target;
        
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c控制台必须指定玩家名！");
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("villagerlimit.stats.others")) {
                sender.sendMessage("§c你没有权限查看其他玩家的统计！");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§c玩家不在线！");
                return true;
            }
        }
        
        TradeStatisticsManager.PlayerTradeStats stats = statsManager.getPlayerStats(target.getUniqueId());
        
        if (stats == null) {
            sender.sendMessage("§e玩家 §6" + target.getName() + " §e还没有交易记录");
            return true;
        }
        
        sender.sendMessage("§6========== §e交易统计 §6==========");
        sender.sendMessage("§e玩家: §6" + stats.playerName);
        sender.sendMessage("§e总交易次数: §6" + stats.totalTrades);
        sender.sendMessage("§e总消耗经验: §6" + stats.totalExpSpent + " §e级");
        sender.sendMessage("§e最常交易物品:");
        
        stats.itemTradeCount.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
            .limit(5)
            .forEach(entry -> {
                sender.sendMessage("  §7- §6" + entry.getKey() + " §7x §6" + entry.getValue());
            });
        
        return true;
    }
}
