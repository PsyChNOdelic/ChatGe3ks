package dev.lsdmc.chatGe3ks.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a player joins the server for the first time across the network
 * This event is cancellable so other plugins can prevent the welcome message and rewards
 */
public class NewPlayerWelcomeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private boolean cancelled;
    private String welcomeMessage;
    private Component welcomeComponent;

    /**
     * Creates a new welcome event for a player joining for the first time
     *
     * @param player The player who joined for the first time
     */
    public NewPlayerWelcomeEvent(Player player) {
        this.player = player;
        this.cancelled = false;
        this.welcomeMessage = null; // Will use default if not set
        this.welcomeComponent = null; // Will use default if not set
    }

    /**
     * Gets the player who joined for the first time
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks if the event is cancelled
     * If cancelled, no welcome message will be sent and no rewards will be processed
     *
     * @return true if the event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether the event is cancelled
     * If cancelled, no welcome message will be sent and no rewards will be processed
     *
     * @param cancelled true to cancel the event
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Sets a custom welcome message to override the random one
     *
     * @param welcomeMessage The message to use, or null to use default
     */
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
        this.welcomeComponent = null; // Reset component when string is set
    }

    /**
     * Gets the custom welcome message if set
     *
     * @return The custom message or null if not set
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * Sets a custom welcome component to override the random one
     * This takes precedence over the string message if both are set
     *
     * @param welcomeComponent The component to use, or null to use default
     */
    public void setWelcomeComponent(Component welcomeComponent) {
        this.welcomeComponent = welcomeComponent;
    }

    /**
     * Gets the custom welcome component if set
     *
     * @return The custom component or null if not set
     */
    public Component getWelcomeComponent() {
        return welcomeComponent;
    }

    /**
     * Checks if a custom welcome message or component is set
     *
     * @return true if a custom message or component is set
     */
    public boolean hasCustomWelcomeMessage() {
        return welcomeMessage != null || welcomeComponent != null;
    }

    /**
     * Checks if a custom welcome component is set
     *
     * @return true if a custom component is set
     */
    public boolean hasCustomWelcomeComponent() {
        return welcomeComponent != null;
    }

    /**
     * Required by Bukkit event system
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit event system (static method)
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}