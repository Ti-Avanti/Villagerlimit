package org.pvp.villagerlimit.optimization;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 村民区块追踪器
 * 优化村民检测，使用区块索引避免重复扫描
 */
public class VillagerChunkTracker extends AbstractModule implements Listener {
    
    // 区块坐标 -> 村民数量
    private final Map<ChunkCoord, Integer> chunkVillagerCount;
    
    public VillagerChunkTracker(Villagerlimit plugin) {
        super(plugin);
        this.chunkVillagerCount = new ConcurrentHashMap<>();
    }
    
    @Override
    public String getName() {
        return "VillagerChunkTracker";
    }
    
    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        // 初始化已加载区块的村民数量
        plugin.getServer().getWorlds().forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                updateChunkVillagerCount(chunk);
            }
        });
        
        info("村民区块追踪器已启用，已追踪 " + chunkVillagerCount.size() + " 个区块");
    }
    
    @Override
    public void onDisable() {
        chunkVillagerCount.clear();
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        updateChunkVillagerCount(event.getChunk());
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        ChunkCoord coord = new ChunkCoord(event.getChunk());
        chunkVillagerCount.remove(coord);
    }
    
    /**
     * 更新区块村民数量
     */
    private void updateChunkVillagerCount(Chunk chunk) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Villager) {
                count++;
            }
        }
        
        ChunkCoord coord = new ChunkCoord(chunk);
        if (count > 0) {
            chunkVillagerCount.put(coord, count);
        } else {
            chunkVillagerCount.remove(coord);
        }
    }
    
    /**
     * 获取区块村民数量
     */
    public int getVillagerCount(Chunk chunk) {
        ChunkCoord coord = new ChunkCoord(chunk);
        return chunkVillagerCount.getOrDefault(coord, 0);
    }
    
    /**
     * 获取区域内村民总数（优化版）
     */
    public int getVillagerCountInRadius(Chunk centerChunk, int radius) {
        int total = 0;
        int centerX = centerChunk.getX();
        int centerZ = centerChunk.getZ();
        String worldName = centerChunk.getWorld().getName();
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                ChunkCoord coord = new ChunkCoord(worldName, x, z);
                total += chunkVillagerCount.getOrDefault(coord, 0);
            }
        }
        
        return total;
    }
    
    /**
     * 增加区块村民数量
     */
    public void incrementVillagerCount(Chunk chunk) {
        ChunkCoord coord = new ChunkCoord(chunk);
        chunkVillagerCount.merge(coord, 1, Integer::sum);
    }
    
    /**
     * 减少区块村民数量
     */
    public void decrementVillagerCount(Chunk chunk) {
        ChunkCoord coord = new ChunkCoord(chunk);
        chunkVillagerCount.computeIfPresent(coord, (k, v) -> {
            int newValue = v - 1;
            return newValue > 0 ? newValue : null;
        });
    }
    
    /**
     * 获取追踪统计信息
     */
    public TrackerStats getStats() {
        int totalChunks = chunkVillagerCount.size();
        int totalVillagers = chunkVillagerCount.values().stream().mapToInt(Integer::intValue).sum();
        return new TrackerStats(totalChunks, totalVillagers);
    }
    
    /**
     * 区块坐标
     */
    private static class ChunkCoord {
        private final String world;
        private final int x;
        private final int z;
        
        public ChunkCoord(Chunk chunk) {
            this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        }
        
        public ChunkCoord(String world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkCoord that)) return false;
            return x == that.x && z == that.z && world.equals(that.world);
        }
        
        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }
    
    /**
     * 追踪统计信息
     */
    public static class TrackerStats {
        public final int trackedChunks;
        public final int totalVillagers;
        
        public TrackerStats(int trackedChunks, int totalVillagers) {
            this.trackedChunks = trackedChunks;
            this.totalVillagers = totalVillagers;
        }
    }
}
