package dev.lsdmc.chatGe3ks;

import dev.lsdmc.chatGe3ks.commands.WelcomeMsgCommand;
import dev.lsdmc.chatGe3ks.data.DataManager;
import dev.lsdmc.chatGe3ks.listeners.ChatListener;
import dev.lsdmc.chatGe3ks.listeners.JoinListener;
import dev.lsdmc.chatGe3ks.messenger.PluginMessenger;
import dev.lsdmc.chatGe3ks.rewards.RewardsManager;
import dev.lsdmc.chatGe3ks.tasks.CleanupTask;
import dev.lsdmc.chatGe3ks.welcome.WelcomeMessagesManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatGe3ks extends JavaPlugin {

    private static ChatGe3ks instance;

    // Managers for handling data, welcome messages, and rewards.
    private DataManager dataManager;
    private WelcomeMessagesManager welcomeMessagesManager;
    private RewardsManager rewardsManager;

    // ChatListener instance for handling chat events (uses config values)
    private ChatListener chatListener;

    // PluginMessenger for plugin messaging (e.g., cross-server)
    private PluginMessenger pluginMessenger;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize DataManager (handles Redis connections and first-join checks)
        dataManager = new DataManager(this);
        dataManager.init();

        // Initialize WelcomeMessagesManager (loads and manages welcome messages)
        welcomeMessagesManager = new WelcomeMessagesManager(this);
        welcomeMessagesManager.loadMessages();

        // Initialize RewardsManager (loads rewards configuration and logic)
        rewardsManager = new RewardsManager(this);
        rewardsManager.loadRewards();

        // Initialize PluginMessenger for cross-server messaging
        pluginMessenger = new PluginMessenger(this);

        // Register commands
        if (getCommand("welcomemsg") != null) {
            getCommand("welcomemsg").setExecutor(new WelcomeMsgCommand(this));
        } else {
            getLogger().warning("Command 'welcomemsg' not defined in plugin.yml!");
        }

        // Register event listeners
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinListener(this, dataManager, welcomeMessagesManager), this);

        // Initialize and register ChatListener instance that uses config values
        chatListener = new ChatListener(this, rewardsManager);
        pm.registerEvents(chatListener, this);

        // Optional integration: Hook into DiscordSRV if available
        if (pm.getPlugin("DiscordSRV") != null) {
            getLogger().info("DiscordSRV detected, initializing Discord integration...");
            // Insert your Discord integration code here.
        }

        // Schedule cleanup task to remove expired welcome window entries.
        // This task runs every 60 seconds (1200 ticks)
        getServer().getScheduler().runTaskTimer(this, new CleanupTask(this), 1200L, 1200L);

        getLogger().info("ChatGe3ks has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown managers and release resources
        if (dataManager != null) {
            dataManager.shutdown();
        }
        getLogger().info("ChatGe3ks has been disabled!");
    }

    /**
     * Provides access to the main plugin instance.
     * @return The ChatGe3ks instance.
     */
    public static ChatGe3ks getInstance() {
        return instance;
    }

    // Getters for our managers and components:
    public DataManager getDataManager() {
        return dataManager;
    }

    public WelcomeMessagesManager getWelcomeMessagesManager() {
        return welcomeMessagesManager;
    }

    public RewardsManager getRewardsManager() {
        return rewardsManager;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public PluginMessenger getPluginMessenger() {
        return pluginMessenger;
    }
}
