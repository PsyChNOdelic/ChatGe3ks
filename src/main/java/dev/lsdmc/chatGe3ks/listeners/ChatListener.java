package dev.lsdmc.chatGe3ks.listeners;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.rewards.RewardsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    private final ChatGe3ks plugin;
    private final RewardsManager rewardsManager;
    // Map to track new joiners and their join timestamps.
    private static final Map<UUID, Long> welcomeWindowMap = new ConcurrentHashMap<>();
    // Welcome window duration in milliseconds, read from config.
    private static long welcomeWindow = 0;

    public ChatListener(ChatGe3ks plugin, RewardsManager rewardsManager) {
        this.plugin = plugin;
        this.rewardsManager = rewardsManager;
        int seconds = plugin.getConfig().getInt("welcome-window", 60);
        welcomeWindow = seconds * 1000L;
    }

    /**
     * Registers a new join time for a player.
     *
     * @param player The newly joined player.
     */
    public void registerNewJoin(Player player) {
        welcomeWindowMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (message.equalsIgnoreCase("welcome")) {
            Player sender = event.getPlayer();
            UUID uuid = sender.getUniqueId();
            Long joinTime = welcomeWindowMap.get(uuid);
            if (joinTime != null && (System.currentTimeMillis() - joinTime) <= welcomeWindow) {
                Bukkit.getScheduler().runTask(plugin, () -> rewardsManager.giveRandomReward(sender));
                welcomeWindowMap.remove(uuid);
            }
        }
    }

    /**
     * Cleans up expired entries from the welcome window map.
     */
    public static void cleanupExpired() {
        long now = System.currentTimeMillis();
        welcomeWindowMap.entrySet().removeIf(entry -> (now - entry.getValue()) > welcomeWindow);
    }
}
