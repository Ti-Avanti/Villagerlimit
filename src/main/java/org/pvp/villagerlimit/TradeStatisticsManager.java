package org.pvp.villagerlimit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TradeStatisticsManager {
    
    private final Villagerlimit plugin;
    private final Map<UUID, PlayerTradeStats> playerStats;
    private List<Map.Entry<UUID, Integer>> leaderboard;
    private long lastLeaderboardUpdate;
    
    public TradeStatisticsManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.playerStats = new ConcurrentHashMap<>();
        this.leaderboard = new ArrayList<>();
        this.lastLeaderboardUpdate = 0;
    }
    
    public void recordTrade(Player player, ItemStack item, int expCost) {
        PlayerTradeStats stats = playerStats.computeIfAbsent(player.getUniqueId(), 
            k -> new PlayerTradeStats(player.getName()));
        
        stats.totalTrades++;
        stats.totalExpSpent += expCost;
        stats.itemTradeCount.merge(item.getType().name(), 1, Integer::sum);
        stats.lastTradeTime = System.currentTimeMillis();
        
        // 记录详细日志
        if (plugin.getLimitConfig().isDetailedLoggingEnabled()) {
            plugin.getLogger().info(String.format(
                "[交易] 玩家: %s, 物品: %s, 消耗经验: %d级",
                player.getName(), item.getType().name(), expCost
            ));
        }
    }
    
    public PlayerTradeStats getPlayerStats(UUID uuid) {
        return playerStats.get(uuid);
    }
    
    public List<Map.Entry<UUID, Integer>> getLeaderboard() {
        VillagerLimitConfig config = plugin.getLimitConfig();
        long updateInterval = config.getLeaderboardUpdateInterval() * 60 * 1000L;
        
        if (System.currentTimeMillis() - lastLeaderboardUpdate > updateInterval) {
            updateLeaderboard();
        }
        
        return leaderboard;
    }
    
    private void updateLeaderboard() {
        leaderboard = playerStats.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().totalTrades, e1.getValue().totalTrades))
            .limit(10)
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().totalTrades))
            .collect(Collectors.toList());
        
        lastLeaderboardUpdate = System.currentTimeMillis();
    }
    
    public void clearData() {
        playerStats.clear();
        leaderboard.clear();
    }
    
    public static class PlayerTradeStats {
        public String playerName;
        public int totalTrades;
        public int totalExpSpent;
        public Map<String, Integer> itemTradeCount;
        public long lastTradeTime;
        
        public PlayerTradeStats(String playerName) {
            this.playerName = playerName;
            this.totalTrades = 0;
            this.totalExpSpent = 0;
            this.itemTradeCount = new HashMap<>();
            this.lastTradeTime = System.currentTimeMillis();
        }
    }
}
