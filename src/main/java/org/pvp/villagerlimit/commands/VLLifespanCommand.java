package org.pvp.villagerlimit.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.VillagerLifespanManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 村民寿命管理命令
 * 用于检测和手动添加村民寿命
 */
public class VLLifespanCommand implements CommandExecutor, TabCompleter {
    
    private final Villagerlimit plugin;
    
    public VLLifespanCommand(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.admin")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§6========== §e村民寿命管理 §6==========");
            sender.sendMessage("§e/vllifespan check §7- 检查没有寿命的村民数量");
            sender.sendMessage("§e/vllifespan add [天数] §7- 为所有无寿命村民添加寿命");
            sender.sendMessage("§e/vllifespan list §7- 列出所有无寿命村民的位置");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "check":
                handleCheck(sender);
                break;
                
            case "add":
                int days = plugin.getLimitConfig().getLifespanDays();
                if (args.length >= 2) {
                    try {
                        days = Integer.parseInt(args[1]);
                        if (days <= 0) {
                            sender.sendMessage("§c天数必须大于0！");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§c无效的天数: " + args[1]);
                        return true;
                    }
                }
                handleAdd(sender, days);
                break;
                
            case "list":
                handleList(sender);
                break;
                
            default:
                sender.sendMessage("§c未知的子命令: " + subCommand);
                sender.sendMessage("§e使用 /vllifespan 查看帮助");
                break;
        }
        
        return true;
    }
    
    /**
     * 检查没有寿命的村民数量
     */
    private void handleCheck(CommandSender sender) {
        VillagerLifespanManager lifespanManager = plugin.getLifespanManager();
        if (lifespanManager == null) {
            sender.sendMessage("§c寿命管理器未初始化！");
            return;
        }
        
        int totalVillagers = 0;
        int villagersWithoutLifespan = 0;
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Villager villager) {
                    totalVillagers++;
                    if (!lifespanManager.hasLifespan(villager)) {
                        villagersWithoutLifespan++;
                    }
                }
            }
        }
        
        sender.sendMessage("§6========== §e村民寿命检查 §6==========");
        sender.sendMessage("§e总村民数: §f" + totalVillagers);
        sender.sendMessage("§e有寿命的村民: §a" + (totalVillagers - villagersWithoutLifespan));
        sender.sendMessage("§e无寿命的村民: §c" + villagersWithoutLifespan);
        
        if (villagersWithoutLifespan > 0) {
            sender.sendMessage("§7使用 §e/vllifespan add [天数] §7为这些村民添加寿命");
        }
    }
    
    /**
     * 为所有无寿命村民添加寿命
     */
    private void handleAdd(CommandSender sender, int days) {
        VillagerLifespanManager lifespanManager = plugin.getLifespanManager();
        if (lifespanManager == null) {
            sender.sendMessage("§c寿命管理器未初始化！");
            return;
        }
        
        if (!plugin.getLimitConfig().isLifespanEnabled()) {
            sender.sendMessage("§c寿命系统未启用！请在配置文件中启用 villager-lifespan.enabled");
            return;
        }
        
        int addedCount = 0;
        
        sender.sendMessage("§e正在为无寿命村民添加寿命...");
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Villager villager) {
                    if (!lifespanManager.hasLifespan(villager)) {
                        lifespanManager.setVillagerLifespan(villager, days);
                        addedCount++;
                    }
                }
            }
        }
        
        sender.sendMessage("§a成功为 §e" + addedCount + " §a个村民添加了 §e" + days + " §a天的寿命！");
        
        if (plugin.getLimitConfig().isDebugEnabled()) {
            plugin.getLogger().info("[寿命管理] " + sender.getName() + " 为 " + addedCount + " 个村民添加了 " + days + " 天寿命");
        }
    }
    
    /**
     * 列出所有无寿命村民的位置
     */
    private void handleList(CommandSender sender) {
        VillagerLifespanManager lifespanManager = plugin.getLifespanManager();
        if (lifespanManager == null) {
            sender.sendMessage("§c寿命管理器未初始化！");
            return;
        }
        
        List<String> locations = new ArrayList<>();
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Villager villager) {
                    if (!lifespanManager.hasLifespan(villager)) {
                        String location = String.format("§e%s §7@ §f%s §7(§f%d, %d, %d§7)",
                            villager.getUniqueId().toString().substring(0, 8),
                            world.getName(),
                            villager.getLocation().getBlockX(),
                            villager.getLocation().getBlockY(),
                            villager.getLocation().getBlockZ()
                        );
                        locations.add(location);
                    }
                }
            }
        }
        
        if (locations.isEmpty()) {
            sender.sendMessage("§a所有村民都已设置寿命！");
            return;
        }
        
        sender.sendMessage("§6========== §e无寿命村民列表 §6==========");
        sender.sendMessage("§7共找到 §e" + locations.size() + " §7个无寿命村民：");
        
        int maxDisplay = 10;
        for (int i = 0; i < Math.min(locations.size(), maxDisplay); i++) {
            sender.sendMessage(locations.get(i));
        }
        
        if (locations.size() > maxDisplay) {
            sender.sendMessage("§7... 还有 §e" + (locations.size() - maxDisplay) + " §7个村民未显示");
        }
        
        sender.sendMessage("§7使用 §e/vllifespan add [天数] §7为这些村民添加寿命");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("check", "add", "list");
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            // add 命令的第二个参数：天数建议
            return Arrays.asList("1", "3", "7", "14", "30");
        }
        
        return completions;
    }
}
