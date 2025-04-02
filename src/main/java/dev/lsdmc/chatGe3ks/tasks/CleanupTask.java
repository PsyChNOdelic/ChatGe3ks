package dev.lsdmc.chatGe3ks.tasks;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.listeners.ChatListener;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;

public class CleanupTask implements Runnable {

    private final ChatGe3ks plugin;
    private final ChatListener chatListener;
    private final LoggerUtils logger;

    /**
     * Creates a new cleanup task for expired welcome entries.
     *
     * @param plugin The main plugin instance
     * @param chatListener The chat listener containing welcome entries
     */
    public CleanupTask(ChatGe3ks plugin, ChatListener chatListener) {
        this.plugin = plugin;
        this.chatListener = chatListener;
        this.logger = plugin.getLoggerUtils();
    }

    @Override
    public void run() {
        try {
            int removedEntries = chatListener.cleanupExpired();

            if (removedEntries > 0) {
                logger.info("Cleaned up " + removedEntries + " expired welcome entries");
            }
        } catch (Exception e) {
            // Catch all exceptions to prevent task cancellation
            logger.error("Error during cleanup task execution", e);
        }
    }
}