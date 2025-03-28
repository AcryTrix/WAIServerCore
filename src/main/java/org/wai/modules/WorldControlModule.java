package org.wai.modules;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

public class WorldControlModule implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private boolean endClosed;
    private boolean netherClosed;

    public WorldControlModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = ((org.wai.WAIServerCore) plugin).getConfigManager();
        this.endClosed = configManager.getBoolean("worldcontrol.end_closed");
        this.netherClosed = configManager.getBoolean("worldcontrol.nether_closed");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("worldcontrol").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("wai.worldcontrol")) {
                sender.sendMessage("§cУ вас нет прав на эту команду!");
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage("§cИспользуйте: /worldcontrol <open|close> <end|nether>");
                return true;
            }

            String action = args[0].toLowerCase();
            String world = args[1].toLowerCase();
            boolean state = action.equals("close");

            switch (world) {
                case "end":
                    endClosed = state;
                    sender.sendMessage("§aДоступ к " + world + " " + (state ? "закрыт" : "открыт"));
                    break;
                case "nether":
                    netherClosed = state;
                    sender.sendMessage("§aДоступ к " + world + " " + (state ? "закрыт" : "открыт"));
                    break;
                default:
                    sender.sendMessage("§cНеизвестный мир: " + world);
                    return true;
            }
            return true;
        });
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
        if (event.getTo() == null) return;
        if (event.getTo().getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END && endClosed) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cДоступ в Край закрыт!");
        } else if (event.getTo().getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER && netherClosed) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cДоступ в Незер закрыт!");
        }
    }
}