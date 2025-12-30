package org.pvp.villagerlimit.display;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

/**
 * 显示适配器接口
 * 用于不同版本的显示实现
 */
public interface DisplayAdapter {
    
    /**
     * 创建显示实体
     */
    Entity createDisplay(Villager villager, String text);
    
    /**
     * 更新显示文本
     */
    void updateDisplay(Entity display, String text);
    
    /**
     * 更新显示位置
     */
    void updateLocation(Entity display, Location location);
    
    /**
     * 移除显示实体
     */
    void removeDisplay(Entity display);
    
    /**
     * 检查显示实体是否有效
     */
    boolean isValid(Entity display);
}
