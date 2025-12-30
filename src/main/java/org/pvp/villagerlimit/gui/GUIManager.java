package org.pvp.villagerlimit.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.pvp.villagerlimit.Villagerlimit;
import org.pvp.villagerlimit.core.AbstractModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI管理器
 * 管理所有GUI界面
 */
public class GUIManager extends AbstractModule implements Listener {
    
    private final Map<UUID, BaseGUI> openGUIs;
    
    public GUIManager(Villagerlimit plugin) {
        super(plugin);
        this.openGUIs = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "GUIManager";
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * 打开GUI
     */
    public void openGUI(Player player, BaseGUI gui) {
        Inventory inventory = gui.createInventory();
        openGUIs.put(player.getUniqueId(), gui);
        player.openInventory(inventory);
    }
    
    /**
     * 关闭GUI
     */
    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
        player.closeInventory();
    }
    
    /**
     * 获取玩家当前打开的GUI
     */
    public BaseGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        
        if (gui != null) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        BaseGUI gui = openGUIs.remove(player.getUniqueId());
        
        if (gui != null) {
            gui.onClose(player);
        }
    }
}
