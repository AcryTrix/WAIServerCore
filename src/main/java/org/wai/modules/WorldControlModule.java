package org.wai.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.wai.config.ConfigManager;

public class WorldControlModule implements Listener {
    private final JavaPlugin plugin;
    private boolean endClosed;
    private boolean netherClosed;

    public WorldControlModule(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.endClosed = configManager.getBoolean("worldcontrol.end_closed");
        this.netherClosed = configManager.getBoolean("worldcontrol.nether_closed");
    }

    public void registerCommandsAndEvents() {
        plugin.getCommand("worldcontrol").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("wai.worldcontrol")) {
                sender.sendMessage("§cУ вас нет прав на управление мирами!");
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
                case "end" -> {
                    endClosed = state;
                    sender.sendMessage("§aЭнд теперь " + (state ? "закрыт" : "открыт") + "!");
                }
                case "nether" -> {
                    netherClosed = state;
                    sender.sendMessage("§aНижний мир теперь " + (state ? "закрыт" : "открыт") + "!");
                }
                default -> sender.sendMessage("§cУкажите мир: end или nether");
            }
            return true;
        });

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
        switch (event.getCause()) {
            case END_PORTAL -> {
                if (endClosed) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cДоступ в Энд временно закрыт!");
                }
            }
            case NETHER_PORTAL -> {
                if (netherClosed) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cДоступ в Нижний мир временно закрыт!");
                }
            }
        }
    }
}