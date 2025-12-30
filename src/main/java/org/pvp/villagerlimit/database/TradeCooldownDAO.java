package org.pvp.villagerlimit.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 交易冷却数据访问对象
 */
public class TradeCooldownDAO {
    
    private final DatabaseManager database;
    
    public TradeCooldownDAO(DatabaseManager database) {
        this.database = database;
    }
    
    /**
     * 保存冷却时间
     */
    public CompletableFuture<Void> saveCooldown(UUID uuid, String profession, String itemType, long cooldownEnd) {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "INSERT OR REPLACE INTO trade_cooldowns " +
                    "(uuid, profession, item_type, cooldown_end) VALUES (?, ?, ?, ?)";
                database.executeUpdate(sql, uuid.toString(), profession, itemType, cooldownEnd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 获取冷却结束时间
     */
    public CompletableFuture<Long> getCooldownEnd(UUID uuid, String profession, String itemType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT cooldown_end FROM trade_cooldowns " +
                    "WHERE uuid = ? AND profession = ? AND item_type = ?";
                ResultSet rs = database.executeQuery(sql, uuid.toString(), profession, itemType);
                
                if (rs.next()) {
                    long cooldownEnd = rs.getLong("cooldown_end");
                    rs.close();
                    return cooldownEnd;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0L;
        });
    }
    
    /**
     * 删除过期的冷却记录
     */
    public CompletableFuture<Void> cleanExpiredCooldowns() {
        return CompletableFuture.runAsync(() -> {
            try {
                String sql = "DELETE FROM trade_cooldowns WHERE cooldown_end < ?";
                database.executeUpdate(sql, System.currentTimeMillis());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 删除玩家冷却数据
     */
    public CompletableFuture<Void> deletePlayerCooldowns(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                database.executeUpdate("DELETE FROM trade_cooldowns WHERE uuid = ?", uuid.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
