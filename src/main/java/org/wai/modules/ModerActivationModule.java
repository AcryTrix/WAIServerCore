package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ModerActivationModule implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final WebhookManager webhookManager;
    private final String codeFilePath;
    private List<String> allowedPlayers;
    private final List<String> activeModerators = new ArrayList<>();

    public ModerActivationModule(JavaPlugin plugin, WebhookManager webhookManager) {
        this.plugin = plugin;
        this.webhookManager = webhookManager;
        this.codeFilePath = plugin.getDataFolder() + "/current_code.txt";
        loadAllowedPlayers();
        startCodeUpdateTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadAllowedPlayers() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        allowedPlayers = plugin.getConfig().getStringList("allowed_players");
        if (allowedPlayers.isEmpty()) {
            plugin.getLogger().warning("Список allowed_players в config.yml пуст или не найден.");
        }
    }

    private String generateCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }

    private void saveCodeToFile(String code) {
        try {
            Files.write(Paths.get(codeFilePath), code.getBytes());
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при записи кода в файл: " + e.getMessage());
        }
    }

    private void startCodeUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String newCode = generateCode();
            saveCodeToFile(newCode);
            webhookManager.sendCodeToDiscord(newCode);
        }, 0L, 20L * 60 * 60);
    }

    public void registerCommands() {
        plugin.getCommand("mon").setExecutor(this);
        plugin.getCommand("moff").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только игрокам!");
            return true;
        }
        Player player = (Player) sender;

        if (label.equalsIgnoreCase("mon")) {
            if (!allowedPlayers.contains(player.getName())) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав на использование этой команды.");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Использование: /mon <код>");
                return true;
            }
            String providedCode = args[0];
            try {
                String currentCode = new String(Files.readAllBytes(Paths.get(codeFilePath))).trim();
                if (currentCode.equals(providedCode)) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            "lp user " + player.getName() + " parent add helper");
                    activeModerators.add(player.getName());
                    sender.sendMessage(ChatColor.GREEN + "Роль helper успешно выдана! Вы в режиме модератора.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Неверный код.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка при чтении файла кода: " + e.getMessage());
                sender.sendMessage(ChatColor.RED + "Произошла ошибка при проверке кода.");
            }
        }
        else if (label.equalsIgnoreCase("moff")) {
            if (!activeModerators.contains(player.getName())) {
                sender.sendMessage(ChatColor.RED + "Вы не находитесь в режиме модератора!");
                return true;
            }
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "lp user " + player.getName() + " parent remove helper");
            activeModerators.remove(player.getName());
            sender.sendMessage(ChatColor.GREEN + "Режим модератора отключен!");
        }
        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (activeModerators.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Вы не можете ломать блоки в режиме модератора!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (activeModerators.contains(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Вы не можете ставить блоки в режиме модератора!");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (activeModerators.contains(player.getName())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Вы не можете атаковать в режиме модератора!");
            }
        }
    }
}