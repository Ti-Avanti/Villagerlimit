package org.pvp.villagerlimit.database;

import org.pvp.villagerlimit.TradeStatisticsManager.PlayerTradeStats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 玩家统计数据访问对象
 */
public class PlayerStatsDAO {
    
    private final DatabaseManager database;
    
    public PlayerStatsDAO(DatabaseManager database) {
        this.database = database;
    }
    
    /**
     * 保存玩家统计
     */
    public CompletableFuture<Void> savePlayerStats(UUID uuid, PlayerTradeStats stats) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "INSERT OR REPLACE INTO player_stats " +
                    "(uuid, player_name, total_trades, total_exp_spent, last_trade_time, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
                
                database.executeUpdate(sql,
                    uuid.toString(),
                    stats.playerName,
                    stats.totalTrades,
                    stats.totalExpSpent,
                    stats.lastTradeTime,
                    System.currentTimeMillis()
                );
                
                // 保存物品交易记录
                for (Map.Entry<String, Integer> entry : stats.itemTradeCount.entrySet()) {
                    saveItemTrade(uuid, entry.getKey(), entry.getValue());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 加载玩家统计
     */
    public CompletableFuture<PlayerTradeStats> loadPlayerStats(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT * FROM player_stats WHERE uuid = ?";
                ResultSet rs = database.executeQuery(sql, uuid.toString());
                
                if (rs.next()) {
                    PlayerTradeStats stats = new PlayerTradeStats(rs.getString("player_name"));
                    stats.totalTrades = rs.getInt("total_trades");
                    stats.totalExpSpent = rs.getInt("total_exp_spent");
                    stats.lastTradeTime = rs.getLong("last_trade_time");
                    
                    // 加载物品交易记录
                    stats.itemTradeCount = loadItemTrades(uuid);
                    
                    rs.close();
                    return stats;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
    
    /**
     * 保存物品交易记录
     */
    private void saveItemTrade(UUID uuid, String itemType, int count) throws SQLException {
        String sql = "INSERT OR REPLACE INTO item_trades (uuid, item_type, trade_count) VALUES (?, ?, ?)";
        database.executeUpdate(sql, uuid.toString(), itemType, count);
    }
    
    /**
     * 加载物品交易记录
     */
    private Map<String, Integer> loadItemTrades(UUID uuid) throws SQLException {
        Map<String, Integer> itemTrades = new HashMap<>();
        String sql = "SELECT item_type, trade_count FROM item_trades WHERE uuid = ?";
        ResultSet rs = database.executeQuery(sql, uuid.toString());
        
        while (rs.next()) {
            itemTrades.put(rs.getString("item_type"), rs.getInt("trade_count"));
        }
        rs.close();
        return itemTrades;
    }
    
    /**
     * 获取排行榜
     */
    public CompletableFuture<List<Map.Entry<UUID, Integer>>> getLeaderboard(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<Map.Entry<UUID, Integer>> leaderboard = new ArrayList<>();
            try {
                String sql = "SELECT uuid, total_trades FROM player_stats ORDER BY total_trades DESC LIMIT ?";
                ResultSet rs = database.executeQuery(sql, limit);
                
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int trades = rs.getInt("total_trades");
                    leaderboard.add(new AbstractMap.SimpleEntry<>(uuid, trades));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return leaderboard;
        });
    }
    
    /**
     * 删除玩家数据
     */
    public CompletableFuture<Void> deletePlayerData(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                database.executeUpdate("DELETE FROM player_stats WHERE uuid = ?", uuid.toString());
                database.executeUpdate("DELETE FROM item_trades WHERE uuid = ?", uuid.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 清空所有数据
     */
    public CompletableFuture<Void> clearAllData() {
        return CompletableFuture.runAsync(() -> {
            try {
                database.executeUpdate("DELETE FROM player_stats");
                database.executeUpdate("DELETE FROM item_trades");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
