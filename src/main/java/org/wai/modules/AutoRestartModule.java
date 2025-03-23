package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AutoRestartModule {
    private final JavaPlugin plugin;
    private final WebhookManager webhookManager;
    private final ConfigManager configManager;
    private int taskId = -1;

    public AutoRestartModule(JavaPlugin plugin, WebhookManager webhookManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
        this.configManager = configManager;
    }

    public void start() {
        scheduleNextRestart();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void scheduleNextRestart() {
        ZoneId zoneId = ZoneId.of(configManager.getString("autorestart.timezone"));
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        List<String> restartTimes = configManager.getStringList("autorestart.times");
        ZonedDateTime nextRestart = findNextRestartTime(now, restartTimes);
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
                Bukkit.broadcastMessage("§eСервер будет перезапущен через 5 минут!");
            }, ticksFiveMinutesBefore);
        }
        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Bukkit.broadcastMessage("§cСервер перезагружается прямо сейчас...");
            Bukkit.shutdown();
        }, ticksUntilRestart);
        Bukkit.getScheduler().runTaskLater(plugin, this::scheduleNextRestart, ticksUntilRestart + 20L);
    }

    private ZonedDateTime findNextRestartTime(ZonedDateTime now, List<String> times) {
        ZonedDateTime next = null;
        for (String time : times) {
            LocalDateTime ldt = LocalDateTime.parse(now.toLocalDate().toString() + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            ZonedDateTime candidate = ldt.atZone(ZoneId.of(configManager.getString("autorestart.timezone")));
            if (candidate.isAfter(now) && (next == null || candidate.isBefore(next))) {
                next = candidate;
            }
        }
        if (next == null) {
            LocalDateTime ldt = LocalDateTime.parse(now.toLocalDate().plusDays(1).toString() + "T" + times.get(0), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            next = ldt.atZone(ZoneId.of(configManager.getString("autorestart.timezone")));
        }
        return next;
    }
}
