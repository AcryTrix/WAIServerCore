package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class AutoRestartModule {
    private final JavaPlugin plugin;
    private final WebhookManager webhookManager;

    public AutoRestartModule(JavaPlugin plugin, WebhookManager webhookManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
    }

    public void start() {
        scheduleNextRestart();
    }

    public void stop() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void scheduleNextRestart() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime nextRestart = getNextRestartTime(now);

        long secondsUntilRestart = ChronoUnit.SECONDS.between(now, nextRestart);
        if (secondsUntilRestart < 0) {
            nextRestart = nextRestart.plusDays(1);
            secondsUntilRestart = ChronoUnit.SECONDS.between(now, nextRestart);
        }

        long ticksUntilRestart = secondsUntilRestart * 20L;
        long ticksFiveMinutesBefore = ticksUntilRestart - (5 * 60 * 20L);

        if (ticksFiveMinutesBefore > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                webhookManager.sendRestartNotification();
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Сервер будет перезагружен через 5 минут!");
            }, ticksFiveMinutesBefore);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "Сервер перезагружается...");
            Bukkit.getServer().shutdown();
            scheduleNextRestart();
        }, ticksUntilRestart);
    }

    private ZonedDateTime getNextRestartTime(ZonedDateTime now) {
        ZonedDateTime next3AM = now.withHour(3).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime next12PM = now.withHour(12).withMinute(0).withSecond(0).withNano(0);

        if (now.isAfter(next12PM)) {
            return next3AM.plusDays(1);
        } else if (now.isAfter(next3AM)) {
            return next12PM;
        } else {
            return next3AM;
        }
    }
}