package org.pvp.villagerlimit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class PermissionGroupManager {
    
    private final Villagerlimit plugin;
    private final List<PermissionGroup> groups;
    
    public PermissionGroupManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.groups = new ArrayList<>();
        loadGroups();
    }
    
    private void loadGroups() {
        groups.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("trade-control.permission-groups");
        
        if (section == null) {
            return;
        }
        
        for (String groupName : section.getKeys(false)) {
            ConfigurationSection groupSection = section.getConfigurationSection(groupName);
            if (groupSection == null) {
                continue;
            }
            
            String permission = groupSection.getString("permission", "");
            int priority = groupSection.getInt("priority", 0);
            double expMultiplier = groupSection.getDouble("exp-cost-multiplier", 1.0);
            double cooldownMultiplier = groupSection.getDouble("cooldown-multiplier", 1.0);
            int limitBonus = groupSection.getInt("daily-limit-bonus", 0);
            
            groups.add(new PermissionGroup(groupName, permission, priority, 
                expMultiplier, cooldownMultiplier, limitBonus));
        }
        
        // 按优先级排序（从高到低）
        groups.sort((g1, g2) -> Integer.compare(g2.priority, g1.priority));
    }
    
    public PermissionGroup getPlayerGroup(Player player) {
        for (PermissionGroup group : groups) {
            if (group.permission.isEmpty() || player.hasPermission(group.permission)) {
                return group;
            }
        }
        
        // 返回默认组
        return groups.stream()
            .filter(g -> g.name.equals("default"))
            .findFirst()
            .orElse(new PermissionGroup("default", "", 0, 1.0, 1.0, 0));
    }
    
    public void reload() {
        loadGroups();
    }
    
    public static class PermissionGroup {
        public final String name;
        public final String permission;
        public final int priority;
        public final double expCostMultiplier;
        public final double cooldownMultiplier;
        public final int dailyLimitBonus;
        
        public PermissionGroup(String name, String permission, int priority,
                             double expCostMultiplier, double cooldownMultiplier, int dailyLimitBonus) {
            this.name = name;
            this.permission = permission;
            this.priority = priority;
            this.expCostMultiplier = expCostMultiplier;
            this.cooldownMultiplier = cooldownMultiplier;
            this.dailyLimitBonus = dailyLimitBonus;
        }
    }
}
