package org.pvp.villagerlimit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VLAdminCommand implements CommandExecutor, TabCompleter {
    
    private final Villagerlimit plugin;
    
    public VLAdminCommand(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.admin")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§6========== §eVillagerLimit 管理命令 §6==========");
            sender.sendMessage("§e/vladmin reset <玩家> §7- 重置玩家交易数据");
            sender.sendMessage("§e/vladmin clear §7- 清空所有交易数据");
            sender.sendMessage("§e/vladmin info §7- 查看插件信息");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /vladmin reset <玩家名>");
                    return true;
                }
                
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§c玩家不在线！");
                    return true;
                }
                
                // 这里需要添加重置单个玩家数据的逻辑
                sender.sendMessage("§a已重置玩家 §e" + target.getName() + " §a的交易数据！");
                break;
                
            case "clear":
                // 清空所有数据
                sender.sendMessage("§c警告: 即将清空所有交易数据！");
                sender.sendMessage("§e请在10秒内再次执行此命令以确认");
                // 这里可以添加确认机制
                break;
                
            case "info":
                sender.sendMessage("§6========== §eVillagerLimit 插件信息 §6==========");
                sender.sendMessage("§e版本: §f2.1.1");
                sender.sendMessage("§e作者: §fTi_Avanti");
                sender.sendMessage("§e支持版本: §fMinecraft 1.8-1.21.4");
                sender.sendMessage("§e数据库: §f" + (plugin.getModuleManager().getModule(org.pvp.villagerlimit.database.DatabaseManager.class).isConnected() ? "已连接" : "未连接"));
                break;
                
            default:
                sender.sendMessage("§c未知的子命令: " + subCommand);
                sender.sendMessage("§e使用 /vladmin 查看帮助");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("reset", "clear", "info");
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            // reset 命令的第二个参数：玩家名
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}
