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
        
        TextDisplay display = villager.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.setBillboard(Display.Billboard.CENTER);
            d.setSeeThrough(true);
            d.setDefaultBackground(false);
            d.text(net.kyori.adventure.text.Component.text(text));
        });
        
        // 让显示实体成为村民的乘客，自动跟随
        villager.addPassenger(display);
        
        return display;
    }
    
    @Override
    public void updateDisplay(Entity entity, String text) {
        if (entity instanceof TextDisplay display) {
            display.text(net.kyori.adventure.text.Component.text(text));
        }
    }
    
    @Override
    public void updateLocation(Entity entity, Location location) {
        // TextDisplay作为乘客会自动跟随，不需要手动更新位置
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
