package org.wai.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.wai.config.ConfigManager;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MOTDModule implements Listener {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final ConfigManager configManager;
    private List<String> messages;
    private final Random random = new Random();

    public MOTDModule(org.bukkit.plugin.java.JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadMessages();
    }

    private void loadMessages() {
        if (!configManager.getBoolean("motd.enabled")) {
            messages = Collections.singletonList("§cMOTD отключен в конфигурации");
            return;
        }
        messages = configManager.getStringList("motd.messages");
        if (messages.isEmpty()) {
            messages = Collections.singletonList("§aДобро пожаловать на сервер!");
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String message = messages.get(random.nextInt(messages.size()));
        event.setMotd(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
    }
}