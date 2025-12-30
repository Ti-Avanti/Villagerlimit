package org.pvp.villagerlimit.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 交易成本递增数据访问对象
 */
public class TradeScalingDAO {
    
    private final DatabaseManager database;
    
    public TradeScalingDAO(DatabaseManager database) {
        this.database = database;
    }
    
    /**
     * 保存交易倍率
     */
    public CompletableFuture<Void> saveScaling(UUID uuid, String itemType, double multiplier, int tradeCount, long lastTradeTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "INSERT OR REPLACE INTO trade_scaling " +
                    "(uuid, item_type, multiplier, trade_count, last_trade_time) VALUES (?, ?, ?, ?, ?)";
                database.executeUpdate(sql, uuid.toString(), itemType, multiplier, tradeCount, lastTradeTime);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 获取交易倍率
     */
    public CompletableFuture<Double> getMultiplier(UUID uuid, String itemType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT multiplier FROM trade_scaling WHERE uuid = ? AND item_type = ?";
                ResultSet rs = database.executeQuery(sql, uuid.toString(), itemType);
                
                if (rs.next()) {
                    double multiplier = rs.getDouble("multiplier");
                    rs.close();
                    return multiplier;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 1.0;
        });
    }
    
    /**
     * 获取交易次数
     */
    public CompletableFuture<Integer> getTradeCount(UUID uuid, String itemType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT trade_count FROM trade_scaling WHERE uuid = ? AND item_type = ?";
                ResultSet rs = database.executeQuery(sql, uuid.toString(), itemType);
                
                if (rs.next()) {
                    int count = rs.getInt("trade_count");
                    rs.close();
                    return count;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
    
    /**
     * 获取最后交易时间
     */
    public CompletableFuture<Long> getLastTradeTime(UUID uuid, String itemType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT last_trade_time FROM trade_scaling WHERE uuid = ? AND item_type = ?";
                ResultSet rs = database.executeQuery(sql, uuid.toString(), itemType);
                
                if (rs.next()) {
                    long time = rs.getLong("last_trade_time");
                    rs.close();
                    return time;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0L;
        });
    }
    
    /**
     * 删除玩家递增数据
     */
    public CompletableFuture<Void> deletePlayerScaling(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                database.executeUpdate("DELETE FROM trade_scaling WHERE uuid = ?", uuid.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 清理过期的递增记录
     */
    public CompletableFuture<Void> cleanExpiredScaling(long expiryTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "DELETE FROM trade_scaling WHERE last_trade_time < ?";
                database.executeUpdate(sql, expiryTime);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
