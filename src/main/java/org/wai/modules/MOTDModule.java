package org.wai.modules;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.wai.WAIServerCore;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MOTDModule implements Listener {
    private final WAIServerCore plugin;
    private List<String> messages;
    private final Random random;

    public MOTDModule(WAIServerCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
        loadMessagesFromResources();
    }

    private void loadMessagesFromResources() {
        File configFile = new File(plugin.getDataFolder(), "motd.yml");

        try {
            if (!configFile.exists()) {
                plugin.getDataFolder().mkdirs();
                try (InputStream is = plugin.getResource("motd.yml")) {
                    Files.copy(is, configFile.toPath());
                }
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            messages = config.getStringList("messages");

            if (messages.isEmpty()) {
                messages = Collections.singletonList("&aDefault MOTD");
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Error loading MOTD config: " + e.getMessage());
            messages = Collections.singletonList("&cError in MOTD config");
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String randomMessage = messages.get(random.nextInt(messages.size()));
        event.setMotd(ChatColor.translateAlternateColorCodes('&', randomMessage));
    }
}