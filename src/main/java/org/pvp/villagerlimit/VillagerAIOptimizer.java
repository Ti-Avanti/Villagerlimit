package org.pvp.villagerlimit;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

import java.util.Collection;
import java.util.List;

public class VillagerAIOptimizer implements Listener {
    
    private final Villagerlimit plugin;
    
    public VillagerAIOptimizer(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Villager && !event.isCancelled()) {
            optimizeVillagerAI((Villager) event.getEntity());
        }
    }
    
    @EventHandler
    public void onZombieVillagerCure(EntityTransformEvent event) {
        if (event.getTransformedEntity() instanceof Villager && !event.isCancelled()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (event.getTransformedEntity() instanceof Villager) {
                    optimizeVillagerAI((Villager) event.getTransformedEntity());
                }
            }, 1L);
        }
    }
    
    private void optimizeVillagerAI(Villager villager) {
        VillagerLimitConfig config = plugin.getLimitConfig();
        
        if (!config.isAIOptimizationEnabled()) {
            return;
        }
        
        List<String> disabledGoals = config.getDisabledGoals();
        
        if (disabledGoals == null || disabledGoals.isEmpty()) {
            return;
        }
        
        try {
            Mob mob = villager;
            Collection<Goal<Mob>> goals = Bukkit.getMobGoals().getAllGoals(mob);
            
            for (Goal<Mob> goal : goals) {
                GoalKey<Mob> key = goal.getKey();
                String goalName = key.getNamespacedKey().getKey().toUpperCase();
                
                for (String disabledGoal : disabledGoals) {
                    if (goalName.contains(disabledGoal.toUpperCase().replace("_", ""))) {
                        Bukkit.getMobGoals().removeGoal(mob, key);
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("优化村民AI时出错: " + e.getMessage());
        }
    }
}
