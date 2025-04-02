package dev.lsdmc.chatGe3ks.listeners;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.rewards.RewardsManager;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final ChatGe3ks plugin;
    private final RewardsManager rewardsManager;
    private final LoggerUtils logger;

    // Map to track new joiners and their join timestamps, using ConcurrentHashMap for thread safety
    private final ConcurrentHashMap<UUID, Long> welcomeWindowMap = new ConcurrentHashMap<>();

    // Welcome window duration in milliseconds
    private final long welcomeWindowDuration;

    // Regex pattern for welcome message detection
    private static final Pattern WELCOME_PATTERN = Pattern.compile("^\\s*welcome\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * Creates a new ChatListener
     *
     * @param plugin The plugin instance
     * @param rewardsManager The rewards manager
     * @param welcomeWindowDurationSeconds Duration of welcome window in seconds
     */
    public ChatListener(ChatGe3ks plugin, RewardsManager rewardsManager, int welcomeWindowDurationSeconds) {
        this.plugin = plugin;
        this.rewardsManager = rewardsManager;
        this.welcomeWindowDuration = Math.max(1, welcomeWindowDurationSeconds) * 1000L;
        this.logger = plugin.getLoggerUtils();

        logger.info("Welcome window duration set to " + welcomeWindowDurationSeconds + " seconds");
    }

    /**
     * Registers a new join time for a player.
     *
     * @param player The newly joined player.
     * @return true if registration was successful
     */
    public boolean registerNewJoin(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        welcomeWindowMap.put(player.getUniqueId(), System.currentTimeMillis());

        // Log this at fine level since it's a common operation
        logger.debug("Registered new player for welcome window: " + player.getName());

        return true;
    }

    /**
     * Handles player chat events to detect welcome messages
     * Uses MONITOR priority to avoid interfering with other chat plugins
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        // Use regex pattern matching for more flexible welcome detection
        if (WELCOME_PATTERN.matcher(message).matches()) {
            processWelcomeMessage(event.getPlayer());
        }
    }

    /**
     * Process a welcome message from a player
     *
     * @param sender The player who sent the welcome message
     */
    private void processWelcomeMessage(final Player sender) {
        if (sender == null || !sender.isOnline()) {
            return;
        }

        final UUID uuid = sender.getUniqueId();
        final Long joinTime = welcomeWindowMap.get(uuid);

        if (joinTime != null) {
            final long currentTime = System.currentTimeMillis();
            final long timeElapsed = currentTime - joinTime;

            if (timeElapsed <= welcomeWindowDuration) {
                // Process reward on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        // Only process if the player is still online
                        if (sender.isOnline()) {
                            boolean rewardGiven = rewardsManager.giveRandomReward(sender);

                            if (rewardGiven) {
                                logger.debug("Gave welcome reward to player: " + sender.getName());
                                // Remove from map regardless of reward success
                                welcomeWindowMap.remove(uuid);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error giving welcome reward to " + sender.getName(), e);
                    }
                });
            } else {
                // Window expired, remove entry
                welcomeWindowMap.remove(uuid);
                logger.debug("Welcome window expired for player: " + sender.getName());
            }
        }
    }

    /**
     * Cleans up expired entries from the welcome window map.
     *
     * @return The number of entries removed
     */
    public int cleanupExpired() {
        int countBefore = welcomeWindowMap.size();

        if (countBefore == 0) {
            return 0;
        }

        long now = System.currentTimeMillis();
        welcomeWindowMap.entrySet().removeIf(entry -> (now - entry.getValue()) > welcomeWindowDuration);

        int countAfter = welcomeWindowMap.size();
        int removed = countBefore - countAfter;

        if (removed > 0) {
            logger.debug("Cleaned up " + removed + " expired welcome entries");
        }

        return removed;
    }

    /**
     * Gets the current size of the welcome window map
     *
     * @return Number of active welcome windows
     */
    public int getActiveWelcomeWindowCount() {
        return welcomeWindowMap.size();
    }

    /**
     * Checks if a player is in their welcome window period
     *
     * @param player The player to check
     * @return true if the player is in their welcome window
     */
    public boolean isPlayerInWelcomeWindow(Player player) {
        if (player == null) {
            return false;
        }

        Long joinTime = welcomeWindowMap.get(player.getUniqueId());
        if (joinTime == null) {
            return false;
        }

        return (System.currentTimeMillis() - joinTime) <= welcomeWindowDuration;
    }

    /**
     * Get the remaining time in the welcome window for a player
     *
     * @param player The player to check
     * @return Remaining time in seconds, or -1 if player is not in welcome window
     */
    public int getRemainingWelcomeWindowTime(Player player) {
        if (player == null) {
            return -1;
        }

        Long joinTime = welcomeWindowMap.get(player.getUniqueId());
        if (joinTime == null) {
            return -1;
        }

        long elapsed = System.currentTimeMillis() - joinTime;
        long remaining = welcomeWindowDuration - elapsed;

        if (remaining <= 0) {
            welcomeWindowMap.remove(player.getUniqueId());
            return -1;
        }

        return (int)(remaining / 1000);
    }
}