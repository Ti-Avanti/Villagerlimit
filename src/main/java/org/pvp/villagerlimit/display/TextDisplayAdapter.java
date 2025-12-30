package org.pvp.villagerlimit.display;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Villager;

/**
 * TextDisplay适配器 (1.19.4+)
 * 使用原版TextDisplay实体
 */
public class TextDisplayAdapter implements DisplayAdapter {
    
    @Override
    public Entity createDisplay(Villager villager, String text) {
        Location loc = villager.getLocation().add(0, villager.getHeight() + 0.5, 0);
        
        return villager.getWorld().spawn(loc, TextDisplay.class, display -> {
            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setDefaultBackground(false);
            display.text(net.kyori.adventure.text.Component.text(text));
        });
    }
    
    @Override
    public void updateDisplay(Entity entity, String text) {
        if (entity instanceof TextDisplay display) {
            display.text(net.kyori.adventure.text.Component.text(text));
        }
    }
    
    @Override
    public void updateLocation(Entity entity, Location location) {
        if (entity instanceof TextDisplay) {
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
        return entity instanceof TextDisplay && !entity.isDead();
    }
}
