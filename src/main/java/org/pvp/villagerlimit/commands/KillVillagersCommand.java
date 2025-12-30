package org.pvp.villagerlimit.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

public class KillVillagersCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villagerlimit.kill")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        int count = 0;
        for (World world : sender.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Villager) {
                    entity.remove();
                    count++;
                }
            }
        }
        
        sender.sendMessage("§a已移除 §e" + count + " §a个村民！");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // killvillagers 命令没有参数
        return new ArrayList<>();
    }
}
