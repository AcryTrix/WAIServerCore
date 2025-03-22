package org.wai.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldControlModule implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private boolean endClosed = true;
    private boolean netherClosed = true;

    public WorldControlModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("worldcontrol").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("wai.worldcontrol")) {
            sender.sendMessage(ChatColor.RED + "Недостаточно прав!");
            return true;
        }

        if (args.length == 2) {
            String action = args[0].toLowerCase();
            String world = args[1].toLowerCase();

            if (action.equals("open") || action.equals("close")) {
                boolean state = action.equals("close");

                switch (world) {
                    case "end":
                        endClosed = state;
                        sender.sendMessage(ChatColor.GREEN + "Энд " + (state ? "закрыт" : "открыт") + "!");
                        return true;
                    case "nether":
                        netherClosed = state;
                        sender.sendMessage(ChatColor.GREEN + "Ад " + (state ? "закрыт" : "открыт") + "!");
                        return true;
                    default:
                        sender.sendMessage(ChatColor.RED + "Доступные миры: end, nether");
                }
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Использование: /worldcontrol <open|close> <end|nether>");
        return true;
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL && endClosed) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Энд временно закрыт!");
        } else if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && netherClosed) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Ад временно закрыт!");
        }
    }
}