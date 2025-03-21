package org.wai.modules.titles.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wai.modules.titles.TitleManager;

public class PlayerListener implements Listener {

    private final TitleManager titleManager;

    public PlayerListener(TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Загрузить титул игрока при входе на сервер
        String playerTitle = titleManager.getPlayerTitle(player);
        if (playerTitle != null) {
            player.sendMessage("Ваш текущий титул: " + playerTitle);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Сохранить данные игрока при выходе
        titleManager.savePlayerData(player);
    }
}