package dev.lsdmc.chatGe3ks.listeners;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.data.DataManager;
import dev.lsdmc.chatGe3ks.event.NewPlayerWelcomeEvent;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import dev.lsdmc.chatGe3ks.welcome.WelcomeMessagesManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Pattern;

public class JoinListener implements Listener {

    private final ChatGe3ks plugin;
    private final DataManager dataManager;
    private final WelcomeMessagesManager welcomeMessagesManager;
    private final LoggerUtils logger;

    // Regex pattern for placeholder replacement
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("\\{player\\}", Pattern.CASE_INSENSITIVE);

    public JoinListener(ChatGe3ks plugin, DataManager dataManager, WelcomeMessagesManager welcomeMessagesManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.welcomeMessagesManager = welcomeMessagesManager;
        this.logger = plugin.getLoggerUtils();
    }

    /**
     * Handles player join events
     * Priority set to NORMAL so other plugins can register earlier/later if needed
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Run Redis check async to not block the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Check if this is the player's first join across the network using Redis
                    final boolean isFirstJoin = dataManager.isFirstJoin(player.getUniqueId());

                    // Switch back to the main thread for event firing and messaging
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (isFirstJoin && player.isOnline()) {
                                handleFirstJoin(player);
                            }
                        }
                    }.runTask(plugin);

                } catch (Exception e) {
                    logger.error("Error checking first join status for " + player.getName(), e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Handles processing for a player's first join
     *
     * @param player The newly joined player
     */
    private void handleFirstJoin(Player player) {
        // Fire a custom welcome event so other plugins can hook in
        NewPlayerWelcomeEvent welcomeEvent = new NewPlayerWelcomeEvent(player);
        plugin.getServer().getPluginManager().callEvent(welcomeEvent);

        if (welcomeEvent.isCancelled()) {
            logger.debug("NewPlayerWelcomeEvent was cancelled by another plugin for " + player.getName());
            return; // Stop further processing if another plugin cancels the event
        }

        // Get welcome message (either custom from event or random)
        String message;
        if (welcomeEvent.hasCustomWelcomeMessage()) {
            message = welcomeEvent.getWelcomeMessage();
        } else {
            message = getWelcomeMessage(player);
        }

        // Check if player is still online before sending message
        if (player.isOnline()) {
            plugin.getMessageUtils().sendInfo(player, message);

            // Register the player in the ChatListener welcome window for reward tracking
            plugin.getChatListener().registerNewJoin(player);

            logger.debug("Sent welcome message to new player: " + player.getName());
        }
    }

    /**
     * Gets and processes a welcome message for the player
     *
     * @param player The player to get a message for
     * @return The processed welcome message
     */
    private String getWelcomeMessage(Player player) {
        // Get a random message from the welcome messages manager
        String message = welcomeMessagesManager.getRandomMessage();

        // Replace player placeholder with player's name using regex for case insensitivity
        message = PLAYER_PLACEHOLDER_PATTERN.matcher(message).replaceAll(player.getName());

        // Apply additional placeholders if needed
        message = applyAdditionalPlaceholders(message, player);

        return message;
    }

    /**
     * Applies additional placeholders to the welcome message
     *
     * @param message The message to process
     * @param player The player for the placeholders
     * @return The processed message
     */
    private String applyAdditionalPlaceholders(String message, Player player) {
        // Apply server-specific placeholders
        message = message.replace("{server}", plugin.getServer().getName());
        message = message.replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));

        // Add any other placeholder replacements here

        return message;
    }
}