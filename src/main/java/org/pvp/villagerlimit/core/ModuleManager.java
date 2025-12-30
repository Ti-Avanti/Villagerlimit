package org.pvp.villagerlimit.core;

import org.pvp.villagerlimit.Villagerlimit;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理器
 * 负责管理所有功能模块的生命周期
 */
public class ModuleManager {
    
    private final Villagerlimit plugin;
    private final List<AbstractModule> modules;
    
    public ModuleManager(Villagerlimit plugin) {
        this.plugin = plugin;
        this.modules = new ArrayList<>();
    }
    
    /**
     * 注册模块
     */
    public void registerModule(AbstractModule module) {
        modules.add(module);
        plugin.getLogger().info("注册模块: " + module.getName());
    }
    
    /**
     * 加载所有模块
     */
    public void loadModules() {
        for (AbstractModule module : modules) {
            try {
                module.onLoad();
            } catch (Exception e) {
                plugin.getLogger().severe("加载模块失败: " + module.getName());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 启用所有模块
     */
    public void enableModules() {
        for (AbstractModule module : modules) {
            try {
                module.onEnable();
                plugin.getLogger().info("启用模块: " + module.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("启用模块失败: " + module.getName());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 禁用所有模块
     */
    public void disableModules() {
        for (int i = modules.size() - 1; i >= 0; i--) {
            AbstractModule module = modules.get(i);
            try {
                module.onDisable();
                plugin.getLogger().info("禁用模块: " + module.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("禁用模块失败: " + module.getName());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 重载所有模块
     */
    public void reloadModules() {
        for (AbstractModule module : modules) {
            try {
                module.onReload();
            } catch (Exception e) {
                plugin.getLogger().severe("重载模块失败: " + module.getName());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 获取指定类型的模块
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractModule> T getModule(Class<T> clazz) {
        for (AbstractModule module : modules) {
            if (clazz.isInstance(module)) {
                return (T) module;
            }
        }
        return null;
    }
    
    /**
     * 获取所有模块
     */
    public List<AbstractModule> getModules() {
        return new ArrayList<>(modules);
    }
}
