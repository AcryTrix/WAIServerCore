package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ModerActivationModule implements Listener {
    private final JavaPlugin plugin;
    private final WebhookManager webhookManager;
    private final ConfigManager configManager;
    private final String codeFilePath;
    private final List<String> allowedPlayers;
    private final List<String> activeModerators = new ArrayList<>();
    private int taskId = -1;

    public ModerActivationModule(JavaPlugin plugin, WebhookManager webhookManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
        this.configManager = configManager;
        this.codeFilePath = plugin.getDataFolder() + "/current_code.txt";
        this.allowedPlayers = configManager.getStringList("moderation.allowed_players");
        startCodeUpdateTask();
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("mon").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда доступна только игрокам!");
                return true;
            }
            if (!allowedPlayers.contains(player.getName())) {
                sender.sendMessage("§cУ вас нет прав на активацию режима модератора!");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("§cИспользуйте: /mon <код>");
                return true;
            }
            activateModerator(player, args[0]);
            return true;
        });

        plugin.getCommand("moff").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cЭта команда доступна только игрокам!");
                return true;
            }
            if (!activeModerators.contains(player.getName())) {
                sender.sendMessage("§cВы не в режиме модератора!");
                return true;
            }
            deactivateModerator(player);
            return true;
        });

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void startCodeUpdateTask() {
        long interval = configManager.getLong("moderation.code_update_interval") * 20L;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            String newCode = generateCode();
            saveCodeToFile(newCode);
            webhookManager.sendCodeToDiscord(newCode);
        }, 0L, interval);
    }

    private String generateCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        return code.toString();
    }

    private void saveCodeToFile(String code) {
        try {
            Files.writeString(Paths.get(codeFilePath), code);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка записи кода в файл: " + e.getMessage());
        }
    }

    private void activateModerator(Player player, String providedCode) {
        try {
            String currentCode = Files.readString(Paths.get(codeFilePath)).trim();
            if (!currentCode.equals(providedCode)) {
                player.sendMessage("§cНеверный код!");
                return;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add helper");
            activeModerators.add(player.getName());
            player.sendMessage("§aРежим модератора активирован!");
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка чтения кода: " + e.getMessage());
            player.sendMessage("§cОшибка при проверке кода!");
        }
    }

    private void deactivateModerator(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent remove helper");
        activeModerators.remove(player.getName());
        player.sendMessage("§aРежим модератора деактивирован!");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (activeModerators.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cЛомать блоки в режиме модератора запрещено!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (activeModerators.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cСтавить блоки в режиме модератора запрещено!");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && activeModerators.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage("§cАтаковать в режиме модератора запрещено!");
        }
    }
}