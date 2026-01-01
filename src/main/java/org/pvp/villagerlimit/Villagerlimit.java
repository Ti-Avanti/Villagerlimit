package org.pvp.villagerlimit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.pvp.villagerlimit.async.AsyncTaskManager;
import org.pvp.villagerlimit.cache.CacheManager;
import org.pvp.villagerlimit.commands.KillVillagersCommand;
import org.pvp.villagerlimit.commands.VLAdminCommand;
import org.pvp.villagerlimit.commands.VLLifespanCommand;
import org.pvp.villagerlimit.commands.VLPerformanceCommand;
import org.pvp.villagerlimit.commands.VLReloadCommand;
import org.pvp.villagerlimit.commands.VLStatsCommand;
import org.pvp.villagerlimit.commands.VLTopCommand;
import org.pvp.villagerlimit.core.LanguageManager;
import org.pvp.villagerlimit.core.ModuleManager;
import org.pvp.villagerlimit.database.DatabaseManager;
import org.pvp.villagerlimit.gui.GUIManager;
import org.pvp.villagerlimit.monitoring.PerformanceMonitor;
import org.pvp.villagerlimit.optimization.MemoryOptimizer;
import org.pvp.villagerlimit.optimization.VillagerChunkTracker;

public final class Villagerlimit extends JavaPlugin {

    private static Villagerlimit instance;
    private ModuleManager moduleManager;
    private VillagerLimitConfig limitConfig;
    private PermissionGroupManager permissionGroupManager;
    private VillagerTradeListener tradeListener;
    private VillagerLifespanManager lifespanManager;

    @Override
    public void onLoad() {
        instance = this;
        
        // 初始化模块管理器
        moduleManager = new ModuleManager(this);
        
        // 注册核心模块
        moduleManager.registerModule(new DatabaseManager(this));
        moduleManager.registerModule(new LanguageManager(this));
        moduleManager.registerModule(new GUIManager(this));
        
        // 注册性能优化模块
        moduleManager.registerModule(new CacheManager(this));
        moduleManager.registerModule(new AsyncTaskManager(this));
        moduleManager.registerModule(new VillagerChunkTracker(this));
        moduleManager.registerModule(new MemoryOptimizer(this));
        moduleManager.registerModule(new PerformanceMonitor(this));
        
        // 加载所有模块
        moduleManager.loadModules();
    }

    @Override
    public void onEnable() {
        // 自动生成配置文件（根据系统语言）
        ConfigGenerator configGenerator = new ConfigGenerator(this);
        configGenerator.generateConfig();
        
        // 启用所有模块
        moduleManager.enableModules();
        
        // 加载配置
        limitConfig = new VillagerLimitConfig(this);
        permissionGroupManager = new PermissionGroupManager(this);
        
        // 初始化寿命管理器
        lifespanManager = new VillagerLifespanManager(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new VillagerSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerSpawnEggListener(this), this);
        getServer().getPluginManager().registerEvents(new ZombieVillagerCureListener(this), this);
        getServer().getPluginManager().registerEvents(new VillagerDeathListener(this), this);
        tradeListener = new VillagerTradeListener(this);
        getServer().getPluginManager().registerEvents(tradeListener, this);
        getServer().getPluginManager().registerEvents(new VillagerAIOptimizer(this), this);
        
        // 注册命令和Tab补全
        if (limitConfig.isKillVillagersEnabled()) {
            KillVillagersCommand killCmd = new KillVillagersCommand();
            getCommand("killvillagers").setExecutor(killCmd);
            getCommand("killvillagers").setTabCompleter(killCmd);
        }
        
        VLReloadCommand reloadCmd = new VLReloadCommand(this);
        getCommand("vlreload").setExecutor(reloadCmd);
        getCommand("vlreload").setTabCompleter(reloadCmd);
        
        VLStatsCommand statsCmd = new VLStatsCommand(this, tradeListener.getStatisticsManager());
        getCommand("vlstats").setExecutor(statsCmd);
        getCommand("vlstats").setTabCompleter(statsCmd);
        
        VLTopCommand topCmd = new VLTopCommand(this, tradeListener.getStatisticsManager());
        getCommand("vltop").setExecutor(topCmd);
        getCommand("vltop").setTabCompleter(topCmd);
        
        VLAdminCommand adminCmd = new VLAdminCommand(this);
        getCommand("vladmin").setExecutor(adminCmd);
        getCommand("vladmin").setTabCompleter(adminCmd);
        
        VLPerformanceCommand perfCmd = new VLPerformanceCommand(this);
        getCommand("vlperf").setExecutor(perfCmd);
        getCommand("vlperf").setTabCompleter(perfCmd);
        
        VLLifespanCommand lifespanCmd = new VLLifespanCommand(this);
        getCommand("vllifespan").setExecutor(lifespanCmd);
        getCommand("vllifespan").setTabCompleter(lifespanCmd);
        
        // 注册 PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VillagerLimitPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI 扩展已注册！");
        }
        
        // 获取语言管理器
        LanguageManager lang = moduleManager.getModule(LanguageManager.class);
        
        getLogger().info("Villagerlimit 插件已成功启用！");
        getLogger().info("配置：");
        getLogger().info("- 语言: " + (lang != null ? lang.getCurrentLanguage() : "未知"));
        getLogger().info("- 村民数量限制: " + (limitConfig.isVillagerLimitEnabled() ? "启用" : "禁用"));
        getLogger().info("- 禁止自然生成: " + (limitConfig.isBlockNaturalSpawn() ? "是" : "否"));
        getLogger().info("- 允许治愈获得: " + (limitConfig.isAllowCure() ? "是" : "否"));
        getLogger().info("- AI优化: " + (limitConfig.isAIOptimizationEnabled() ? "启用" : "禁用"));
        getLogger().info("- 禁用交易: " + (limitConfig.isDisableTrading() ? "是" : "否"));
        getLogger().info("- 数据库: " + (moduleManager.getModule(DatabaseManager.class).isConnected() ? "已连接" : "未连接"));
    }

    @Override
    public void onDisable() {
        // 清理寿命管理器
        if (lifespanManager != null) {
            lifespanManager.cleanup();
        }
        
        // 禁用所有模块
        if (moduleManager != null) {
            moduleManager.disableModules();
        }
        
        getLogger().info("Villagerlimit 插件已关闭！");
    }
    
    public static Villagerlimit getInstance() {
        return instance;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public VillagerLimitConfig getLimitConfig() {
        return limitConfig;
    }
    
    public PermissionGroupManager getPermissionGroupManager() {
        return permissionGroupManager;
    }
    
    public VillagerTradeListener getTradeListener() {
        return tradeListener;
    }
    
    public VillagerLifespanManager getLifespanManager() {
        return lifespanManager;
    }
}
