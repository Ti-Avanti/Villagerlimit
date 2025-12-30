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
        
        return villager.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(text);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setInvulnerable(true);
            armorStand.setCollidable(false);
        });
    }
    
    @Override
    public void updateDisplay(Entity entity, String text) {
        if (entity instanceof ArmorStand armorStand) {
            armorStand.setCustomName(text);
        }
    }
    
    @Override
    public void updateLocation(Entity entity, Location location) {
        if (entity instanceof ArmorStand) {
            entity.teleport(location);
        }
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
