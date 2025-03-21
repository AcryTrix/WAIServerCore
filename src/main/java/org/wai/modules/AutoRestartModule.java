package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class AutoRestartModule {
    private final JavaPlugin plugin;
    private final WebhookManager webhookManager;
    private int taskId;
    private int lastRestartHour = -1;

    public AutoRestartModule(JavaPlugin plugin, WebhookManager webhookManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
    }

    public void start() {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            ZoneId moscowZone = ZoneId.of("Europe/Moscow");
            ZonedDateTime now = ZonedDateTime.now(moscowZone);
            int hour = now.getHour();
            int minute = now.getMinute();

            if ((hour == 3 || hour == 12) && minute == 0) {
                if (lastRestartHour != hour) {
                    lastRestartHour = hour;
                    Bukkit.getScheduler().runTask(plugin, this::restartServer);
                }
            }
        }, 0L, 1200L).getTaskId();
    }

    private void restartServer() {
        webhookManager.sendRestartNotification();
        Bukkit.broadcastMessage("§cServer will restart in 5 seconds...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }, 100L);
    }

    public void instantRestart() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.broadcastMessage("§cСервер будет перезагружен немедленно по команде администратора.");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
        });
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}