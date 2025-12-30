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
        TradeStatisticsManager statsManager = plugin.getTradeListener().getStatisticsManager();
        
        // 处理排行榜变量（不需要玩家在线）
        if (params.startsWith("top_")) {
            return handleTopPlaceholder(params, statsManager);
        }
        
        // 其他变量需要玩家在线
        if (player == null || !player.isOnline()) {
            return "";
        }
        
        Player p = player.getPlayer();
        if (p == null) {
            return "";
        }
        
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
    
    /**
     * 处理排行榜变量
     * 格式: top_<排名>_<类型>
     * 例如: top_1_name, top_1_trades, top_1_exp
     */
    private String handleTopPlaceholder(String params, TradeStatisticsManager statsManager) {
        String[] parts = params.split("_");
        if (parts.length < 3) {
            return "";
        }
        
        try {
            int rank = Integer.parseInt(parts[1]);
            String type = parts[2].toLowerCase();
            
            var leaderboard = statsManager.getLeaderboard();
            if (rank < 1 || rank > leaderboard.size()) {
                return "-";
            }
            
            var entry = leaderboard.get(rank - 1);
            TradeStatisticsManager.PlayerTradeStats stats = statsManager.getPlayerStats(entry.getKey());
            
            if (stats == null) {
                return "-";
            }
            
            switch (type) {
                case "name":
                    return stats.playerName;
                case "trades":
                    return String.valueOf(stats.totalTrades);
                case "exp":
                    return String.valueOf(stats.totalExpSpent);
                default:
                    return "";
            }
        } catch (NumberFormatException e) {
            return "";
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
