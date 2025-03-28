package org.wai.modules;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

import java.util.EnumMap;
import java.util.Map;

public class EntityTrackerModule implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public EntityTrackerModule(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        if (plugin.getCommand("entitytracker") != null) {
            plugin.getCommand("entitytracker").setExecutor(this);
        } else {
            plugin.getLogger().severe("Не удалось зарегистрировать команду 'entitytracker'");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!configManager.getBoolean("entity_tracker.enabled")) {
            sender.sendMessage("§cМодуль отслеживания сущностей отключен в конфиге.");
            return true;
        }
        if (!sender.hasPermission("entitytracker.use")) {
            sender.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        trackEntities(sender);
        return true;
    }

    private void trackEntities(CommandSender sender) {
        Map<EntityType, Integer> entityCount = new EnumMap<>(EntityType.class);
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                entityCount.merge(entity.getType(), 1, Integer::sum);
            }
        }
        StringBuilder message = new StringBuilder("§aОтслеживание сущностей на сервере:\n");
        entityCount.forEach((type, count) ->
                message.append("§7- ").append(type.name()).append(": §f").append(count).append("\n"));
        sender.sendMessage(message.toString());
    }

    public void register() {
        if (configManager.getBoolean("entity_tracker.enabled")) {
            plugin.getLogger().info("Модуль отслеживания сущностей зарегистрирован.");
        }
    }
}