package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

import java.util.EnumMap;
import java.util.Map;

public class EntityTrackerModule {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private int taskId = -1;

    public EntityTrackerModule(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void start() {
        if (!configManager.getBoolean("entity_tracker.enabled")) {
            plugin.getLogger().info("Модуль отслеживания сущностей отключен в конфиге.");
            return;
        }

        long interval = configManager.getLong("entity_tracker.log_interval") * 20L;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::trackEntities, 20L, interval);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void trackEntities() {
        Map<EntityType, Integer> entityCount = new EnumMap<>(EntityType.class);

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                entityCount.merge(entity.getType(), 1, Integer::sum);
            }
        }

        StringBuilder log = new StringBuilder("§aОтслеживание сущностей на сервере:\n");
        entityCount.forEach((type, count) ->
                log.append("§7- ").append(type.name()).append(": §f").append(count).append("\n"));

        plugin.getLogger().info(log.toString());
    }
}