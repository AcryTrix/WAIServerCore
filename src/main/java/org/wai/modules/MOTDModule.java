package org.wai.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.wai.WAIServerCore;
import java.io.File;
import java.io.IOException;

public class MOTDModule implements Listener, CommandExecutor {
    private final WAIServerCore plugin;
    private String motd;
    private File configFile;
    private YamlConfiguration config;

    public MOTDModule(WAIServerCore plugin) {
        this.plugin = plugin;
        setupConfig();
        loadConfig();
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("setmotd").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "motd.yml");
        if (!configFile.exists()) {
            plugin.saveResource("motd.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void loadConfig() {
        motd = config.getString("motd", "&aWelcome to the server!");
    }

    private void saveMotdToConfig(String newMotd) {
        config.set("motd", newMotd);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save MOTD to config: " + e.getMessage());
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String formattedMotd = ChatColor.translateAlternateColorCodes('&', motd);
        event.setMotd(formattedMotd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wai.setmotd")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /setmotd <message>");
            return true;
        }
        String newMotd = String.join(" ", args);
        motd = newMotd;
        saveMotdToConfig(newMotd);
        sender.sendMessage(ChatColor.GREEN + "MOTD set to: " + ChatColor.RESET + newMotd);
        return true;
    }
}