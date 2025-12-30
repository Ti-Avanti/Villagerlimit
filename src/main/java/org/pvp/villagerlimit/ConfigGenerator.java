package org.pvp.villagerlimit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 配置文件生成器
 * 根据服务器语言自动生成对应语言的配置文件
 */
public class ConfigGenerator {
    
    private final Villagerlimit plugin;
    
    public ConfigGenerator(Villagerlimit plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 生成配置文件
     * 根据系统语言自动选择中文或英文注释
     */
    public void generateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        
        // 如果配置文件已存在，不覆盖，直接返回
        if (configFile.exists()) {
            plugin.getLogger().info("Config file already exists, skipping generation");
            return;
        }
        
        // 确保数据文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // 检测系统语言
        String systemLanguage = detectSystemLanguage();
        plugin.getLogger().info("Detected system language: " + systemLanguage);
        
        // 根据语言选择配置模板
        String templateName = systemLanguage.startsWith("zh") ? "config.yml" : "config_en.yml";
        
        // 复制配置文件
        try {
            copyConfigFromResource(templateName, configFile);
            plugin.getLogger().info("Generated config file with " + 
                (systemLanguage.startsWith("zh") ? "Chinese" : "English") + " comments");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to generate config file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检测系统语言
     */
    private String detectSystemLanguage() {
        // 优先使用 JVM 语言设置
        String language = System.getProperty("user.language");
        String country = System.getProperty("user.country");
        
        if (language != null) {
            if (country != null) {
                return language + "_" + country;
            }
            return language;
        }
        
        // 使用 Locale
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry();
    }
    
    /**
     * 从资源文件复制配置
     */
    private void copyConfigFromResource(String resourceName, File targetFile) throws IOException {
        // 确保父目录存在
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        
        // 从资源读取
        InputStream inputStream = plugin.getResource(resourceName);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourceName);
        }
        
        // 写入文件
        try (OutputStream outputStream = new FileOutputStream(targetFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    /**
     * 更新配置文件语言
     * 用于已存在的配置文件
     */
    public void updateConfigLanguage() {
        FileConfiguration config = plugin.getConfig();
        String configLanguage = config.getString("language", "zh_CN");
        String systemLanguage = detectSystemLanguage();
        
        // 如果配置语言与系统语言不匹配，提示用户
        if (!configLanguage.startsWith(systemLanguage.substring(0, 2))) {
            plugin.getLogger().info("Config language (" + configLanguage + 
                ") differs from system language (" + systemLanguage + ")");
            plugin.getLogger().info("You can change the 'language' setting in config.yml");
        }
    }
}
