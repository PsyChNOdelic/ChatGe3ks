package dev.lsdmc.chatGe3ks.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NewPlayerWelcomeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private boolean cancelled;

    public NewPlayerWelcomeEvent(Player player) {
        this.player = player;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
