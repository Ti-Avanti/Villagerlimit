package org.pvp.villagerlimit;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class VillagerSpawnListener implements Listener {
    
    private final Villagerlimit plugin;
    
    public VillagerSpawnListener(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.VILLAGER) {
            return;
        }
        
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        // 如果配置为禁止自然生成，则取消所有自然生成
        if (config.isBlockNaturalSpawn()) {
            event.setCancelled(true);
        }
    }
}