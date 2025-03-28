package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ReportModule {
    private final JavaPlugin plugin;
    private final WebhookManager reportWebhookManager;

    public ReportModule(JavaPlugin plugin, WebhookManager reportWebhookManager) {
        this.plugin = plugin;
        this.reportWebhookManager = reportWebhookManager;
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
        String timestamp = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault()).format(Instant.now());

        // Получаем координаты репортера
        Location location = reporter.getLocation();
        String worldName = switch (location.getWorld().getEnvironment()) {
            case NORMAL -> "Обычный мир";
            case NETHER -> "Ад";
            case THE_END -> "Край";
            default -> "Неизвестный мир";
        };
        String coordinates = String.format("X: %.1f, Y: %.1f, Z: %.1f (%s)",
                location.getX(), location.getY(), location.getZ(), worldName);

        // JSON-пайлоад для Discord
        String jsonPayload = String.format(
                "{\"embeds\":[{\"title\":\"Новый репорт\",\"description\":\"**От:** %s\\n**На:** %s\\n**Причина:** %s\\n**Координаты:** %s\",\"color\":16711680,\"footer\":{\"text\":\"%s\"}}]}",
                reporter.getName(), target.getName(), reason, coordinates, timestamp
        );

        // Отправка в Discord
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (reportWebhookManager.getWebhookUrl() != null) {
                reportWebhookManager.sendAsyncWebhook(jsonPayload);
            }
        });

        // Уведомление игроку и модераторам
        reporter.sendMessage("§aРепорт отправлен модераторам!");
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("waiservercore.reports.view"))
                .forEach(p -> p.sendMessage("§c[Репорт] " + reporter.getName() + " -> " + target.getName() + ": " + reason + " §7[Координаты: " + coordinates + "]"));
    }

    public String getWebhookUrl() {
        return reportWebhookManager.getWebhookUrl();
    }
}
