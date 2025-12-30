package org.pvp.villagerlimit;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * 村民死亡监听器
 * 清理死亡村民的显示实体
 */
public class VillagerDeathListener implements Listener {
    
    private final Villagerlimit plugin;
    
    public VillagerDeathListener(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onVillagerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        
        // 清理寿命显示
        VillagerLifespanManager lifespanManager = plugin.getLifespanManager();
        if (lifespanManager != null && lifespanManager.hasLifespan(villager)) {
            lifespanManager.cleanupVillagerDisplay(villager);
        }
    }
}
