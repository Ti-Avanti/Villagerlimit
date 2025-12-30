package org.pvp.villagerlimit.display;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

/**
 * ArmorStand适配器 (1.8+)
 * 使用盔甲架显示文本
 */
public class ArmorStandAdapter implements DisplayAdapter {
    
    @Override
    public Entity createDisplay(Villager villager, String text) {
        Location loc = villager.getLocation().add(0, villager.getHeight() + 0.3, 0);
        
        ArmorStand armorStand = villager.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setCustomNameVisible(true);
            as.setCustomName(text);
            as.setMarker(true);
            as.setSmall(true);
            as.setInvulnerable(true);
            as.setCollidable(false);
        });
        
        // 让盔甲架成为村民的乘客，自动跟随
        villager.addPassenger(armorStand);
        
        return armorStand;
    }
    
    @Override
    public void updateDisplay(Entity entity, String text) {
        if (entity instanceof ArmorStand armorStand) {
            armorStand.setCustomName(text);
        }
    }
    
    @Override
    public void updateLocation(Entity entity, Location location) {
        // ArmorStand作为乘客会自动跟随，不需要手动更新位置
    }
    
    @Override
    public void removeDisplay(Entity entity) {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }
    
    @Override
    public boolean isValid(Entity entity) {
        return entity instanceof ArmorStand && !entity.isDead();
    }
}
