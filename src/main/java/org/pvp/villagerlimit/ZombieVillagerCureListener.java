package org.pvp.villagerlimit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class ZombieVillagerCureListener implements Listener {
    
    private final Villagerlimit plugin;
    
    public ZombieVillagerCureListener(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        if (event.getEntity().getType() != EntityType.ZOMBIE_VILLAGER || 
            event.getTransformedEntity().getType() != EntityType.VILLAGER) {
            return;
        }
        
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        // 如果不允许治愈，直接取消
        if (!config.isAllowCure()) {
            event.setCancelled(true);
            return;
        }
        
        // 如果启用了数量限制，检查区域内村民数量
        if (config.isVillagerLimitEnabled()) {
            Location location = event.getEntity().getLocation();
            int nearbyVillagers = countNearbyVillagers(location, config.getChunkRadius());
            
            if (nearbyVillagers >= config.getMaxVillagers()) {
                event.setCancelled(true);
                
                // 通知附近玩家
                Player nearestPlayer = location.getWorld().getNearbyPlayers(location, 10).stream()
                    .findFirst()
                    .orElse(null);
                
                if (nearestPlayer != null) {
                    nearestPlayer.sendMessage(config.getLimitMessage());
                }
                return;
            }
        }
        
        // 设置村民寿命
        if (config.isLifespanEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event.getTransformedEntity() instanceof Villager villager) {
                    int lifespanDays = config.getVillagerLifespanDays();
                    plugin.getLifespanManager().setVillagerLifespan(villager, lifespanDays);
                    
                    // 通知附近玩家
                    if (config.isLifespanNotifyEnabled()) {
                        String message = config.getLifespanSetMessage()
                            .replace("{days}", String.valueOf(lifespanDays));
                        
                        int range = config.getLifespanNotifyRange();
                        if (range <= 0) {
                            // 全服通知
                            plugin.getServer().getOnlinePlayers()
                                .forEach(player -> player.sendMessage(message));
                        } else {
                            // 范围通知
                            villager.getWorld().getNearbyPlayers(villager.getLocation(), range)
                                .forEach(player -> player.sendMessage(message));
                        }
                    }
                }
            }, 1L);
        }
    }
    
    private int countNearbyVillagers(Location location, int chunkRadius) {
        int count = 0;
        Chunk centerChunk = location.getChunk();
        int centerX = centerChunk.getX();
        int centerZ = centerChunk.getZ();
        
        for (int x = centerX - chunkRadius; x <= centerX + chunkRadius; x++) {
            for (int z = centerZ - chunkRadius; z <= centerZ + chunkRadius; z++) {
                Chunk chunk = location.getWorld().getChunkAt(x, z);
                for (org.bukkit.entity.Entity entity : chunk.getEntities()) {
                    if (entity instanceof Villager) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
}