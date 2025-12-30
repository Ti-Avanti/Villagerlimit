package org.pvp.villagerlimit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.pvp.villagerlimit.core.LanguageManager;

import java.util.Map;

/**
 * 村民刷怪蛋监听器
 * 控制使用刷怪蛋放置村民
 */
public class VillagerSpawnEggListener implements Listener {
    
    private final Villagerlimit plugin;
    
    public VillagerSpawnEggListener(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onVillagerSpawnFromEgg(CreatureSpawnEvent event) {
        // 只处理村民
        if (event.getEntityType() != EntityType.VILLAGER) {
            return;
        }
        
        // 只处理刷怪蛋生成
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            return;
        }
        
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        // 如果未启用村民限制，允许放置
        if (!config.isVillagerLimitEnabled()) {
            return;
        }
        
        // 检查是否允许使用刷怪蛋放置
        if (!config.isAllowSpawnEgg()) {
            event.setCancelled(true);
            
            // 通知附近的玩家
            Location location = event.getLocation();
            Player nearestPlayer = location.getWorld().getNearbyPlayers(location, 10).stream()
                .findFirst()
                .orElse(null);
            
            if (nearestPlayer != null) {
                LanguageManager lang = plugin.getModuleManager().getModule(LanguageManager.class);
                if (lang != null) {
                    nearestPlayer.sendMessage(lang.getMessage("villager.spawn-egg-blocked"));
                } else {
                    nearestPlayer.sendMessage("§c禁止使用村民刷怪蛋！");
                }
            }
            return;
        }
        
        // 检查区域村民数量
        if (isVillagerLimitReached(event.getLocation(), config)) {
            event.setCancelled(true);
            
            // 通知附近的玩家
            Location location = event.getLocation();
            Player nearestPlayer = location.getWorld().getNearbyPlayers(location, 10).stream()
                .findFirst()
                .orElse(null);
            
            if (nearestPlayer != null) {
                LanguageManager lang = plugin.getModuleManager().getModule(LanguageManager.class);
                if (lang != null) {
                    nearestPlayer.sendMessage(lang.getMessage("villager.limit-reached"));
                } else {
                    nearestPlayer.sendMessage("§c该区域村民数量已达上限！");
                }
            }
            return;
        }
        
        // 为刷怪蛋放置的村民设置寿命
        boolean debug = config.isDebugEnabled();
        if (config.isLifespanEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event.getEntity() instanceof Villager villager) {
                    int lifespanDays = config.getVillagerLifespanDays();
                    if (debug) {
                        plugin.getLogger().info("[寿命调试] 刷怪蛋村民设置寿命: " + lifespanDays + " 天");
                    }
                    plugin.getLifespanManager().setVillagerLifespan(villager, lifespanDays);
                    
                    // 通知附近玩家
                    if (config.isLifespanNotifyEnabled()) {
                        LanguageManager lang = plugin.getModuleManager().getModule(LanguageManager.class);
                        String message;
                        if (lang != null) {
                            message = lang.getMessage("lifespan.set", 
                                Map.of("days", lifespanDays));
                        } else {
                            message = "§a村民已获得 " + lifespanDays + " 天寿命！";
                        }
                        
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
    
    /**
     * 检查区域村民数量是否达到上限
     */
    private boolean isVillagerLimitReached(Location location, VillagerLimitConfig config) {
        int radius = config.getChunkRadius();
        int maxVillagers = config.getMaxVillagers();
        
        Chunk centerChunk = location.getChunk();
        int villagerCount = 0;
        
        // 检查周围区块的村民数量
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Chunk chunk = location.getWorld().getChunkAt(
                    centerChunk.getX() + x,
                    centerChunk.getZ() + z
                );
                
                // 统计该区块的村民数量
                for (var entity : chunk.getEntities()) {
                    if (entity instanceof Villager) {
                        villagerCount++;
                    }
                }
            }
        }
        
        return villagerCount >= maxVillagers;
    }
}
