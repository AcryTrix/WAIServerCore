package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookManager {
    private final JavaPlugin plugin;
    private final String webhookUrl;

    public WebhookManager(JavaPlugin plugin, String webhookUrl) {
        this.plugin = plugin;
        this.webhookUrl = webhookUrl;
    }

    public void sendServerStartMessage() {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        String jsonPayload = "{\"embeds\":[{\"title\":\"Статус сервера\",\"description\":\"✅ Сервер успешно запущен!\",\"color\":65280,\"fields\":[{\"name\":\"Игроки\",\"value\":\"0/" + Bukkit.getMaxPlayers() + "\",\"inline\":true},{\"name\":\"Версия\",\"value\":\"" + Bukkit.getVersion() + "\",\"inline\":true}]}]}";
        sendAsyncWebhook(jsonPayload);
    }

    public void sendRestartNotification() {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        String jsonPayload = "{\"embeds\":[{\"title\":\"Перезапуск сервера\",\"description\":\"⚠️ Запланированный перезапуск сервера начат!\",\"color\":16753920}]}";
        sendAsyncWebhook(jsonPayload);
    }

    private void sendAsyncWebhook(String jsonPayload) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                int responseCode = connection.getResponseCode();
                if (responseCode != 204) {
                    plugin.getLogger().warning("Не удалось отправить вебхук: код " + responseCode);
                }
                connection.disconnect();
            } catch (IOException e) {
                plugin.getLogger().warning("Ошибка отправки вебхука: " + e.getMessage());
            }
        });
    }
}