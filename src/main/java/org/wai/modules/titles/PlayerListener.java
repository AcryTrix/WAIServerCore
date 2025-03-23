package org.wai.modules.titles;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final TitleManager titleManager;

    public PlayerListener(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        titleManager.applyTitle(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        titleManager.savePlayerData(event.getPlayer());
    }
}