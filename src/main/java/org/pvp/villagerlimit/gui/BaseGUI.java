package org.pvp.villagerlimit.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.pvp.villagerlimit.Villagerlimit;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGUI {
    
    protected final Villagerlimit plugin;
    protected final Player player;
    protected Inventory inventory;
    
    public BaseGUI(Villagerlimit plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    public abstract String getTitle();
    public abstract int getSize();
    public abstract void createContents();
    public abstract void handleClick(InventoryClickEvent event);
    
    public void onClose(Player player) {
    }
    
    public Inventory createInventory() {
        inventory = Bukkit.createInventory(null, getSize(), getTitle());
        createContents();
        return inventory;
    }
    
    public void refresh() {
        inventory.clear();
        createContents();
    }
    
    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line.replace("&", "§"));
                }
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    protected ItemStack createItem(Material material, int amount, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        item.setAmount(amount);
        return item;
    }
    
    protected void fillBorder(Material material) {
        ItemStack borderItem = createItem(material, " ");
        
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(getSize() - 9 + i, borderItem);
        }
        
        for (int i = 9; i < getSize() - 9; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }
}
