package org.wai.modules.titles;

import org.bukkit.entity.Player;

public class TradeRequest {
    private final Player sender; // Игрок, который отправил запрос
    private final Player target; // Игрок, которому отправлен запрос

    public TradeRequest(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
    }

    public Player getSender() {
        return sender;
    }

    public Player getTarget() {
        return target;
    }
}