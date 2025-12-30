package org.pvp.villagerlimit;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VillagerLimitPlaceholderExpansion extends PlaceholderExpansion {
    
    private final Villagerlimit plugin;
    
    public VillagerLimitPlaceholderExpansion(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "villagerlimit";
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "PvP";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) {
            return "";
        }
        
        Player p = player.getPlayer();
        if (p == null) {
            return "";
        }
        
        TradeStatisticsManager statsManager = plugin.getTradeListener().getStatisticsManager();
        TradeStatisticsManager.PlayerTradeStats stats = statsManager.getPlayerStats(p.getUniqueId());
        
        switch (params.toLowerCase()) {
            case "trades":
                return stats != null ? String.valueOf(stats.totalTrades) : "0";
                
            case "exp_spent":
                return stats != null ? String.valueOf(stats.totalExpSpent) : "0";
                
            case "rank":
                int rank = getRank(p);
                return rank > 0 ? String.valueOf(rank) : "-";
                
            case "group":
                PermissionGroupManager.PermissionGroup group = plugin.getPermissionGroupManager().getPlayerGroup(p);
                return group.name;
                
            case "group_priority":
                PermissionGroupManager.PermissionGroup g = plugin.getPermissionGroupManager().getPlayerGroup(p);
                return String.valueOf(g.priority);
                
            case "daily_limit":
                int limit = plugin.getLimitConfig().getGlobalTradeLimit();
                PermissionGroupManager.PermissionGroup pg = plugin.getPermissionGroupManager().getPlayerGroup(p);
                return String.valueOf(limit + pg.dailyLimitBonus);
                
            case "daily_used":
                TradeLimitManager limitManager = plugin.getTradeListener().getLimitManager();
                return String.valueOf(limitManager.getGlobalTradeCount(p));
                
            case "daily_remaining":
                TradeLimitManager lm = plugin.getTradeListener().getLimitManager();
                int used = lm.getGlobalTradeCount(p);
                int max = plugin.getLimitConfig().getGlobalTradeLimit();
                PermissionGroupManager.PermissionGroup grp = plugin.getPermissionGroupManager().getPlayerGroup(p);
                int remaining = (max + grp.dailyLimitBonus) - used;
                return String.valueOf(Math.max(0, remaining));
                
            default:
                return null;
        }
    }
    
    private int getRank(Player player) {
        var leaderboard = plugin.getTradeListener().getStatisticsManager().getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getKey().equals(player.getUniqueId())) {
                return i + 1;
            }
        }
        return -1;
    }
}
