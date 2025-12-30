package org.pvp.villagerlimit.database;

import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库管理器
 * 使用SQLite存储数据
 */
public class DatabaseManager extends AbstractModule {
    
    private Connection connection;
    private final File databaseFile;
    
    public DatabaseManager(Villagerlimit plugin) {
        super(plugin);
        this.databaseFile = new File(plugin.getDataFolder(), "data.db");
    }
    
    @Override
    public String getName() {
        return "DatabaseManager";
    }
    
    @Override
    public void onLoad() {
        try {
            // 加载SQLite驱动
            Class.forName("org.sqlite.JDBC");
            
            // 创建数据库连接
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            
            // 创建表
            createTables();
            
            info("数据库连接成功");
        } catch (Exception e) {
            error("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            error("关闭数据库连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建数据库表
     */
    private void createTables() throws SQLException {
        // 玩家交易统计表
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS player_stats (" +
            "uuid VARCHAR(36) PRIMARY KEY," +
            "player_name VARCHAR(16) NOT NULL," +
            "total_trades INTEGER DEFAULT 0," +
            "total_exp_spent INTEGER DEFAULT 0," +
            "last_trade_time BIGINT DEFAULT 0," +
            "created_at BIGINT DEFAULT 0," +
            "updated_at BIGINT DEFAULT 0" +
            ")"
        );
        
        // 物品交易记录表
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS item_trades (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "uuid VARCHAR(36) NOT NULL," +
            "item_type VARCHAR(64) NOT NULL," +
            "trade_count INTEGER DEFAULT 0," +
            "UNIQUE(uuid, item_type)" +
            ")"
        );
        
        // 交易冷却表
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS trade_cooldowns (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "uuid VARCHAR(36) NOT NULL," +
            "profession VARCHAR(32) NOT NULL," +
            "item_type VARCHAR(64) NOT NULL," +
            "cooldown_end BIGINT NOT NULL," +
            "UNIQUE(uuid, profession, item_type)" +
            ")"
        );
        
        // 交易次数限制表
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS trade_limits (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "uuid VARCHAR(36) NOT NULL," +
            "limit_type VARCHAR(32) NOT NULL," +
            "limit_key VARCHAR(64) NOT NULL," +
            "count INTEGER DEFAULT 0," +
            "reset_date VARCHAR(10) NOT NULL," +
            "UNIQUE(uuid, limit_type, limit_key, reset_date)" +
            ")"
        );
        
        // 交易成本递增表
        executeUpdate(
            "CREATE TABLE IF NOT EXISTS trade_scaling (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "uuid VARCHAR(36) NOT NULL," +
            "item_type VARCHAR(64) NOT NULL," +
            "multiplier DOUBLE DEFAULT 1.0," +
            "trade_count INTEGER DEFAULT 0," +
            "last_trade_time BIGINT DEFAULT 0," +
            "UNIQUE(uuid, item_type)" +
            ")"
        );
        
        info("数据库表创建完成");
    }
    
    /**
     * 执行更新语句
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }
    
    /**
     * 执行查询语句
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }
    
    /**
     * 异步执行更新
     */
    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeUpdate(sql, params);
            } catch (SQLException e) {
                error("异步更新失败: " + e.getMessage());
                return 0;
            }
        });
    }
    
    /**
     * 异步执行查询
     */
    public CompletableFuture<ResultSet> executeQueryAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeQuery(sql, params);
            } catch (SQLException e) {
                error("异步查询失败: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 获取数据库连接
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * 检查连接是否有效
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
