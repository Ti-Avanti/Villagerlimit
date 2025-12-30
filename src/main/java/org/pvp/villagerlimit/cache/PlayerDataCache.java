package org.pvp.villagerlimit.cache;

import java.util.UUID;

/**
 * 玩家数据缓存
 */
public class PlayerDataCache {
    
    private final UUID uuid;
    private final String playerName;
    private int totalTrades;
    private int totalExpSpent;
    private long lastTradeTime;
    private long expireTime;
    
    public PlayerDataCache(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.totalTrades = 0;
        this.totalExpSpent = 0;
        this.lastTradeTime = 0;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getTotalTrades() {
        return totalTrades;
    }
    
    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }
    
    public void incrementTrades() {
        this.totalTrades++;
    }
    
    public int getTotalExpSpent() {
        return totalExpSpent;
    }
    
    public void setTotalExpSpent(int totalExpSpent) {
        this.totalExpSpent = totalExpSpent;
    }
    
    public void addExpSpent(int exp) {
        this.totalExpSpent += exp;
    }
    
    public long getLastTradeTime() {
        return lastTradeTime;
    }
    
    public void setLastTradeTime(long lastTradeTime) {
        this.lastTradeTime = lastTradeTime;
    }
    
    public long getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}
