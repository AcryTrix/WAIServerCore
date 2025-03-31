package org.wai.modules;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.wai.WAIServerCore;
import org.wai.config.ConfigManager;
import org.bukkit.Particle;

public class VapeModule {
    private final WAIServerCore plugin;
    private final ConfigManager configManager;
    private Particle particleType;
    private int effectDuration;

    public VapeModule(WAIServerCore plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        // Исправленная строка:
        String particleName = configManager.getString("vape.particle", "CAMPFIRE_COSY_SMOKE");

        try {
            particleType = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            particleType = Particle.CAMPFIRE_COSY_SMOKE;
            plugin.getLogger().warning("Неверный тип частиц: " + particleName + ". Используется значение по умолчанию.");
        }

        effectDuration = configManager.getInt("vape.duration", 60);
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("vape").setExecutor(new VapeCommand());
    }

    private class VapeCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Команда только для игроков!");
                return true;
            }

            Player player = (Player) sender;
            startVapeEffect(player);
            return true;
        }
    }

    private void startVapeEffect(Player player) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= effectDuration) {
                    this.cancel();
                    return;
                }

                Vector direction = player.getLocation().getDirection().normalize();
                Location particleLoc = player.getLocation()
                        .add(0, 1.5, 0)
                        .add(direction.multiply(0.5));

                player.getWorld().spawnParticle(
                        particleType,
                        particleLoc,
                        15,
                        0.1,
                        0.1,
                        0.1,
                        0.02
                );

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}