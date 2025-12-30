package org.pvp.villagerlimit.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 交易次数限制数据访问对象
 */
public class TradeLimitDAO {
    
    private final DatabaseManager database;
    
    public TradeLimitDAO(DatabaseManager database) {
        this.database = database;
    }
    
    /**
     * 保存交易次数
     */
    public CompletableFuture<Void> saveTradeCount(UUID uuid, String limitType, String limitKey, int count, String resetDate) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "INSERT OR REPLACE INTO trade_limits " +
                    "(uuid, limit_type, limit_key, count, reset_date) VALUES (?, ?, ?, ?, ?)";
                database.executeUpdate(sql, uuid.toString(), limitType, limitKey, count, resetDate);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 获取交易次数
     */
    public CompletableFuture<Integer> getTradeCount(UUID uuid, String limitType, String limitKey, String resetDate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT count FROM trade_limits " +
                    "WHERE uuid = ? AND limit_type = ? AND limit_key = ? AND reset_date = ?";
                ResultSet rs = database.executeQuery(sql, uuid.toString(), limitType, limitKey, resetDate);
                
                if (rs.next()) {
                    int count = rs.getInt("count");
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
     * 增加交易次数
     */
    public CompletableFuture<Void> incrementTradeCount(UUID uuid, String limitType, String limitKey, String resetDate) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "INSERT INTO trade_limits (uuid, limit_type, limit_key, count, reset_date) " +
                    "VALUES (?, ?, ?, 1, ?) " +
                    "ON CONFLICT(uuid, limit_type, limit_key, reset_date) " +
                    "DO UPDATE SET count = count + 1";
                database.executeUpdate(sql, uuid.toString(), limitType, limitKey, resetDate);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 清理过期的限制记录
     */
    public CompletableFuture<Void> cleanExpiredLimits(String currentDate) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "DELETE FROM trade_limits WHERE reset_date < ?";
                database.executeUpdate(sql, currentDate);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 删除玩家限制数据
     */
    public CompletableFuture<Void> deletePlayerLimits(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                database.executeUpdate("DELETE FROM trade_limits WHERE uuid = ?", uuid.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
