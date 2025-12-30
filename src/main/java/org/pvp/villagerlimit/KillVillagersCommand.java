package org.pvp.villagerlimit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Villager;

public class KillVillagersCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.kill")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        int killedCount = 0;
        
        for (World world : Bukkit.getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                villager.remove();
                killedCount++;
            }
        }
        
        sender.sendMessage("§a成功移除了 §e" + killedCount + " §a个村民！");
        return true;
    }
}