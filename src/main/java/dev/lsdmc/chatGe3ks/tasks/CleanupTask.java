package dev.lsdmc.chatGe3ks.tasks;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.listeners.ChatListener;

public class CleanupTask implements Runnable {

    private final ChatGe3ks plugin;

    public CleanupTask(ChatGe3ks plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ChatListener.cleanupExpired();
        plugin.getLogger().info("CleanupTask: Cleaned up expired welcome entries.");
    }
}
