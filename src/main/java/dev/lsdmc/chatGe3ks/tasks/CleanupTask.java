package dev.lsdmc.chatGe3ks.tasks;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.listeners.ChatListener;

import java.util.logging.Level;

public class CleanupTask implements Runnable {

    private final ChatGe3ks plugin;
    private final ChatListener chatListener;

    /**
     * Creates a new cleanup task for expired welcome entries.
     *
     * @param plugin The main plugin instance
     * @param chatListener The chat listener containing welcome entries
     */
    public CleanupTask(ChatGe3ks plugin, ChatListener chatListener) {
        this.plugin = plugin;
        this.chatListener = chatListener;
    }

    @Override
    public void run() {
        try {
            int removedEntries = chatListener.cleanupExpired();

            if (removedEntries > 0) {
                plugin.getLogger().info("CleanupTask: Removed " + removedEntries + " expired welcome entries.");
            }
        } catch (Exception e) {
            // Catch all exceptions to prevent task cancellation
            plugin.getLogger().log(Level.SEVERE, "Error during cleanup task execution", e);
        }
    }
}