package org.pvp.villagerlimit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.pvp.villagerlimit.Villagerlimit;

public class VLReloadCommand implements CommandExecutor {
    
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
        
        try {
            plugin.reloadConfig();
            plugin.getLimitConfig().reload();
            
            // 重载所有模块
            if (plugin.getModuleManager() != null) {
                plugin.getModuleManager().reloadModules();
            }
            
            sender.sendMessage("§a配置已重载！");
        } catch (Exception e) {
            sender.sendMessage("§c重载配置时出错: " + e.getMessage());
            plugin.getLogger().warning("重载配置失败: " + e.getMessage());
        }
        
        return true;
    }
}
