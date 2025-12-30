package org.pvp.villagerlimit.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.pvp.villagerlimit.Villagerlimit;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理器
 * 支持多语言消息系统
 */
public class LanguageManager extends AbstractModule {
    
    private String currentLanguage;
    private FileConfiguration languageConfig;
    private final Map<String, String> messageCache;
    
    public LanguageManager(Villagerlimit plugin) {
        super(plugin);
        this.messageCache = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "LanguageManager";
    }
    
    @Override
    public void onLoad() {
        // 从配置读取语言设置
        currentLanguage = plugin.getConfig().getString("language", "zh_CN");
        loadLanguageFile();
    }
    
    @Override
    public void onReload() {
        messageCache.clear();
        currentLanguage = plugin.getConfig().getString("language", "zh_CN");
        loadLanguageFile();
    }
    
    /**
     * 加载语言文件
     */
    private void loadLanguageFile() {
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        File langFile = new File(langFolder, currentLanguage + ".yml");
        
        // 如果文件不存在，从资源复制
        if (!langFile.exists()) {
            plugin.saveResource("languages/" + currentLanguage + ".yml", false);
        }
        
        // 加载语言文件
        languageConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // 加载默认语言作为后备
        InputStream defStream = plugin.getResource("languages/" + currentLanguage + ".yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defStream, StandardCharsets.UTF_8)
            );
            languageConfig.setDefaults(defConfig);
        }
        
        info("已加载语言: " + currentLanguage);
    }
    
    /**
     * 获取消息
     */
    public String getMessage(String key) {
        if (messageCache.containsKey(key)) {
            return messageCache.get(key);
        }
        
        String message = languageConfig.getString(key);
        if (message == null) {
            message = key;
        }
        
        // 处理颜色代码
        message = message.replace("&", "§");
        
        messageCache.put(key, message);
        return message;
    }
    
    /**
     * 获取消息并替换占位符
     */
    public String getMessage(String key, Object... replacements) {
        String message = getMessage(key);
        
        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(replacements[i]));
        }
        
        return message;
    }
    
    /**
     * 获取消息并替换命名占位符
     */
    public String getMessage(String key, Map<String, Object> replacements) {
        String message = getMessage(key);
        
        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        
        return message;
    }
    
    /**
     * 获取当前语言
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
