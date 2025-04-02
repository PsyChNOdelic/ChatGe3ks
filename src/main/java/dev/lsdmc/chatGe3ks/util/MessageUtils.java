package dev.lsdmc.chatGe3ks.util;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class for handling plugin messages using Adventure API
 */
public class MessageUtils {

    private final ChatGe3ks plugin;
    private final BukkitAudiences adventure;
    private final MiniMessage miniMessage;

    public MessageUtils(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.adventure = BukkitAudiences.create(plugin);
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Sends a formatted message to a player or console
     *
     * @param sender The recipient
     * @param message The message text
     * @param type The message type (error, success, info)
     */
    public void sendMessage(CommandSender sender, String message, MessageType type) {
        String prefix = plugin.getConfig().getString("messages.prefix",
                Constants.Chat.PREFIX_MINI_MESSAGE);

        // Convert prefix using MiniMessage format
        Component prefixComponent = miniMessage.deserialize(prefix);

        // Format based on message type
        Component messageComponent;
        switch (type) {
            case ERROR:
                messageComponent = Component.text(message).color(NamedTextColor.RED);
                break;
            case SUCCESS:
                messageComponent = Component.text(message).color(NamedTextColor.GREEN);
                break;
            case INFO:
                messageComponent = Component.text(message).color(NamedTextColor.YELLOW);
                break;
            case HELP:
                messageComponent = Component.text(message).color(NamedTextColor.GRAY);
                break;
            default:
                messageComponent = Component.text(message);
        }

        // Combine prefix and message
        Component fullMessage = prefixComponent.append(messageComponent);

        // Send message
        adventure.sender(sender).sendMessage(fullMessage);
    }

    /**
     * Sends an error message
     *
     * @param sender The recipient
     * @param message The message text
     */
    public void sendError(CommandSender sender, String message) {
        sendMessage(sender, message, MessageType.ERROR);
    }

    /**
     * Sends a success message
     *
     * @param sender The recipient
     * @param message The message text
     */
    public void sendSuccess(CommandSender sender, String message) {
        sendMessage(sender, message, MessageType.SUCCESS);
    }

    /**
     * Sends an info message
     *
     * @param sender The recipient
     * @param message The message text
     */
    public void sendInfo(CommandSender sender, String message) {
        sendMessage(sender, message, MessageType.INFO);
    }

    /**
     * Sends a help message
     *
     * @param sender The recipient
     * @param message The message text
     */
    public void sendHelp(CommandSender sender, String message) {
        sendMessage(sender, message, MessageType.HELP);
    }

    /**
     * Sends a formatted component message
     *
     * @param sender The recipient
     * @param component The component to send
     */
    public void sendComponent(CommandSender sender, Component component) {
        adventure.sender(sender).sendMessage(component);
    }

    /**
     * Parses a string using MiniMessage format
     *
     * @param message The message to parse
     * @return The parsed component
     */
    public Component parse(String message) {
        return miniMessage.deserialize(message);
    }

    /**
     * Broadcasts a message to all online players
     *
     * @param message The message text
     * @param type The message type
     */
    public void broadcastMessage(String message, MessageType type) {
        String prefix = plugin.getConfig().getString("messages.prefix",
                Constants.Chat.PREFIX_MINI_MESSAGE);

        // Convert prefix using MiniMessage format
        Component prefixComponent = miniMessage.deserialize(prefix);

        // Format based on message type
        Component messageComponent;
        switch (type) {
            case ERROR:
                messageComponent = Component.text(message).color(NamedTextColor.RED);
                break;
            case SUCCESS:
                messageComponent = Component.text(message).color(NamedTextColor.GREEN);
                break;
            case INFO:
                messageComponent = Component.text(message).color(NamedTextColor.YELLOW);
                break;
            case HELP:
                messageComponent = Component.text(message).color(NamedTextColor.GRAY);
                break;
            default:
                messageComponent = Component.text(message);
        }

        // Combine prefix and message
        Component fullMessage = prefixComponent.append(messageComponent);

        // Broadcast to all players
        adventure.players().sendMessage(fullMessage);

        // Also log to console (strip formatting)
        plugin.getLogger().info(plainText(fullMessage));
    }

    /**
     * Converts a component to plain text
     *
     * @param component The component to convert
     * @return Plain text representation
     */
    public String plainText(Component component) {
        return MiniMessage.miniMessage().serialize(component)
                .replaceAll("<[^>]*>", ""); // Remove all tags
    }

    /**
     * Closes the Adventure API audience
     */
    public void close() {
        if (adventure != null) {
            adventure.close();
        }
    }

    /**
     * Enum for message types
     */
    public enum MessageType {
        ERROR,
        SUCCESS,
        INFO,
        HELP,
        NORMAL
    }
}