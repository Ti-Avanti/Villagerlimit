package org.pvp.villagerlimit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.ArrayList;
import java.util.List;

public class VLReloadCommand implements CommandExecutor, TabCompleter {
    
    private final Villagerlimit plugin;
    
    public VLReloadCommand(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.admin")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        sender.sendMessage("§e正在重载配置...");
        
        try {
            // 重载配置文件
            plugin.reloadConfig();
            
            // 重新加载配置类
            plugin.getLimitConfig().reload();
            plugin.getPermissionGroupManager().reload();
            
            sender.sendMessage("§a配置重载成功！");
        } catch (Exception e) {
            sender.sendMessage("§c配置重载失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // vlreload 命令没有参数
        return new ArrayList<>();
    }
}
