package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ReportModule {
    private final JavaPlugin plugin;
    private final WebhookManager reportWebhookManager;
    private final File reportsFile;

    public ReportModule(JavaPlugin plugin, WebhookManager reportWebhookManager) {
        this.plugin = plugin;
        this.reportWebhookManager = reportWebhookManager;
        this.reportsFile = new File(plugin.getDataFolder(), "reports.log");
        registerCommand();
    }

    private void registerCommand() {
        plugin.getCommand("report").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда только для игроков!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cИспользуйте: /report <ник> <причина>");
                return true;
            }
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cИгрок " + targetName + " не найден!");
                return true;
            }
            if (target.equals(player)) {
                sender.sendMessage("§cВы не можете пожаловаться на себя!");
                return true;
            }
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            handleReport(player, target, reason);
            return true;
        });
    }

    private void handleReport(Player reporter, Player target, String reason) {
        String timestamp = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.now());
        String logEntry = String.format("%s | %s -> %s: %s%n", timestamp, reporter.getName(), target.getName(), reason);
        String jsonPayload = String.format("{\"embeds\":[{\"title\":\"Новый репорт\",\"description\":\"**От:** %s\\n**На:** %s\\n**Причина:** %s\",\"color\":16711680,\"footer\":{\"text\":\"%s\"}}]}", reporter.getName(), target.getName(), reason, timestamp);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileWriter writer = new FileWriter(reportsFile, true)) {
                writer.write(logEntry);
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка записи репорта: " + e.getMessage());
            }
            reportWebhookManager.sendAsyncWebhook(jsonPayload);
        });
        reporter.sendMessage("§aРепорт отправлен модераторам!");
        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("waiservercore.reports.view")).forEach(p -> p.sendMessage("§c[Репорт] " + reporter.getName() + " -> " + target.getName() + ": " + reason));
    }
}