package org.wai.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private final JavaPlugin plugin;
    private TomlParseResult config;
    private final Path configPath;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configPath = Paths.get(plugin.getDataFolder().getPath(), "config.toml");
        if (!Files.exists(plugin.getDataFolder().toPath())) {
            plugin.getDataFolder().mkdirs();
        }
        loadConfig();
    }

    public void loadConfig() {
        if (!Files.exists(configPath)) {
            try {
                plugin.saveResource("config.toml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Failed to save default config.toml: " + e.getMessage());
            }
        }
        try {
            config = Toml.parse(configPath);
            if (config.hasErrors()) {
                config.errors().forEach(error -> plugin.getLogger().severe("Ошибка в config.toml: " + error.toString()));
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось загрузить config.toml: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public TomlParseResult getTomlConfig() {
        return config;
    }

    public String getString(String key) {
        return config != null ? config.getString(key) : null;
    }

    public boolean getBoolean(String key) {
        if (config == null) return false;
        Boolean value = config.getBoolean(key);
        return value != null && value;
    }

    public long getLong(String key) {
        if (config == null) return 0L;
        Long value = config.getLong(key);
        return value != null ? value : 0L;
    }

    public java.util.List<String> getStringList(String key) {
        return config != null && config.getArray(key) != null ? config.getArray(key).toList().stream()
                .map(Object::toString)
                .toList() : java.util.Collections.emptyList();
    }
}