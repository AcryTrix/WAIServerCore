package org.wai.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {
    private final JavaPlugin plugin;
    private TomlParseResult tomlConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadTomlConfig();
    }

    private void loadTomlConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.toml");
        if (!configFile.exists()) {
            plugin.saveResource("config.toml", false);
        }
        try {
            tomlConfig = Toml.parse(configFile.toPath());
            if (tomlConfig.hasErrors()) {
                tomlConfig.errors().forEach(error ->
                        plugin.getLogger().warning("Config error: " + error.toString()));
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load config.toml: " + e.getMessage());
            tomlConfig = null;
        }
    }

    public TomlParseResult getTomlConfig() {
        return tomlConfig;
    }

    public String getString(String path) {
        return tomlConfig != null && tomlConfig.contains(path) ? tomlConfig.getString(path) : null;
    }

    public boolean getBoolean(String path) {
        return tomlConfig != null && tomlConfig.contains(path) && tomlConfig.getBoolean(path);
    }

    public long getLong(String path) {
        return tomlConfig != null && tomlConfig.contains(path) ? tomlConfig.getLong(path) : 0;
    }

    public List<String> getStringList(String path) {
        return tomlConfig != null && tomlConfig.contains(path) && tomlConfig.isArray(path) ?
                tomlConfig.getArray(path).toList().stream().map(Object::toString).collect(Collectors.toList()) :
                Collections.emptyList();
    }
}