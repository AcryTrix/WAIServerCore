package org.wai.modules.titles;

import org.bukkit.entity.Player;

public class TradeRequest {
    private final Player sender;
    private final Player target;

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