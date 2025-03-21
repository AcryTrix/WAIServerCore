package org.wai.modules.titles;

import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        String playerTitle = titleManager.getPlayerTitle(player);
        if (playerTitle != null) {
            player.sendMessage("Ваш текущий титул: " + playerTitle);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        titleManager.savePlayerData(event.getPlayer());
    }
}