package dev.lsdmc.chatGe3ks.util;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class for handling plugin messages
 */
public class MessageUtils {

    private final ChatGe3ks plugin;

    public MessageUtils(ChatGe3ks plugin) {
        this.plugin = plugin;
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
                Constants.Chat.PREFIX);

        // Translate color codes
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        // Format based on message type
        String formattedMessage;
        switch (type) {
            case ERROR:
                formattedMessage = prefix + ChatColor.RED + message;
                break;
            case SUCCESS:
                formattedMessage = prefix + ChatColor.GREEN + message;
                break;
            case INFO:
                formattedMessage = prefix + ChatColor.YELLOW + message;
                break;
            case HELP:
                formattedMessage = prefix + ChatColor.GRAY + message;
                break;
            default:
                formattedMessage = prefix + message;
        }

        sender.sendMessage(formattedMessage);
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
     * Broadcasts a message to all online players
     *
     * @param message The message text
     * @param type The message type
     */
    public void broadcastMessage(String message, MessageType type) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendMessage(player, message, type);
        }

        // Also log to console
        plugin.getLogger().info(ChatColor.stripColor(message));
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