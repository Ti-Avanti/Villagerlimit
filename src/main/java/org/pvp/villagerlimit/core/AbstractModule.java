package org.pvp.villagerlimit.core;

import org.pvp.villagerlimit.Villagerlimit;

/**
 * 模块基类
 * 所有功能模块都应继承此类
 */
public abstract class AbstractModule {
    
    protected final Villagerlimit plugin;
    private boolean enabled = false;
    
    public AbstractModule(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 模块加载时调用
     */
    public void onLoad() {
        // 子类可重写
    }
    
    /**
     * 模块启用时调用
     */
    public void onEnable() {
        this.enabled = true;
    }
    
    /**
     * 模块禁用时调用
     */
    public void onDisable() {
        this.enabled = false;
    }
    
    /**
     * 重载模块配置
     */
    public void onReload() {
        // 子类可重写
    }
    
    /**
     * 获取模块名称
     */
    public abstract String getName();
    
    /**
     * 模块是否已启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 记录信息日志
     */
    protected void info(String message) {
        plugin.getLogger().info("[" + getName() + "] " + message);
    }
    
    /**
     * 记录警告日志
     */
    protected void warn(String message) {
        plugin.getLogger().warning("[" + getName() + "] " + message);
    }
    
    /**
     * 记录错误日志
     */
    protected void error(String message) {
        plugin.getLogger().severe("[" + getName() + "] " + message);
    }
}
