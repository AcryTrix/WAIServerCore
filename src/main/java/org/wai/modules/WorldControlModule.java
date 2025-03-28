package org.wai.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

    private final JavaPlugin plugin;

        this.plugin = plugin;
    }

    public void registerCommandsAndEvents() {
        if (!sender.hasPermission("wai.worldcontrol")) {
            return true;
        }

            String action = args[0].toLowerCase();
            String world = args[1].toLowerCase();
                boolean state = action.equals("close");

                switch (world) {
                        endClosed = state;
                        netherClosed = state;
                        return true;

    }

    @EventHandler
    public void onPortalUse(PlayerPortalEvent event) {
            event.setCancelled(true);
            event.setCancelled(true);
        }
    }
}