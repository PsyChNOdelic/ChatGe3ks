package dev.lsdmc.chatGe3ks;

import dev.lsdmc.chatGe3ks.commands.CommandManager;
import dev.lsdmc.chatGe3ks.data.DataManager;
import dev.lsdmc.chatGe3ks.listeners.ChatListener;
import dev.lsdmc.chatGe3ks.listeners.JoinListener;
import dev.lsdmc.chatGe3ks.messenger.PluginMessenger;
import dev.lsdmc.chatGe3ks.rewards.RewardsManager;
import dev.lsdmc.chatGe3ks.tasks.CleanupTask;
import dev.lsdmc.chatGe3ks.util.ConfigValidator;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import dev.lsdmc.chatGe3ks.util.MessageUtils;
import dev.lsdmc.chatGe3ks.welcome.WelcomeMessagesManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatGe3ks extends JavaPlugin {

    // Managers
    private DataManager dataManager;
    private WelcomeMessagesManager welcomeMessagesManager;
    private RewardsManager rewardsManager;
    private CommandManager commandManager;
    private PluginMessenger pluginMessenger;
    private ChatListener chatListener;

    // Utility classes
    private ConfigValidator configValidator;
    private LoggerUtils loggerUtils;
    private MessageUtils messageUtils;

    // Adventure API
    private BukkitAudiences adventure;

    // Config values
    private int welcomeWindowDuration;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize Adventure API
        this.adventure = BukkitAudiences.create(this);

        // Initialize utility classes
        loggerUtils = new LoggerUtils(this);
        messageUtils = new MessageUtils(this);

        // Log startup
        loggerUtils.logStartup();

        // Initialize configuration validator
        configValidator = new ConfigValidator(this);
        if (!configValidator.validateConfig()) {
            loggerUtils.warning("Invalid configuration detected. Using safe defaults where possible.");
        }

        // Load config values
        loadConfigValues();

        // Initialize managers
        initializeManagers();

        // Register event listeners
        registerEventListeners();

        // Register commands
        registerCommands();

        // Schedule tasks
        scheduleTasks();

        // Optional integrations
        setupIntegrations();

        loggerUtils.info("ChatGe3ks has been enabled!");
    }

    private void loadConfigValues() {
        welcomeWindowDuration = getConfig().getInt(Constants.Config.WELCOME_WINDOW,
                (int) Constants.Time.DEFAULT_WELCOME_WINDOW_SECONDS);
        loggerUtils.info("Welcome window duration set to " + welcomeWindowDuration + " seconds");
    }

    private void initializeManagers() {
        // Initialize DataManager
        dataManager = new DataManager(this);
        if (!dataManager.init()) {
            loggerUtils.warning("Failed to initialize Redis connection - first join detection may not work properly");
        }

        // Initialize WelcomeMessagesManager
        welcomeMessagesManager = new WelcomeMessagesManager(this);
        welcomeMessagesManager.loadMessages();

        // Initialize RewardsManager
        rewardsManager = new RewardsManager(this);
        rewardsManager.loadRewards();

        // Initialize PluginMessenger
        pluginMessenger = new PluginMessenger(this);

        // Initialize CommandManager
        commandManager = new CommandManager(this);
    }

    private void registerEventListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        // Register JoinListener
        pm.registerEvents(new JoinListener(this, dataManager, welcomeMessagesManager), this);

        // Initialize and register ChatListener
        chatListener = new ChatListener(this, rewardsManager, welcomeWindowDuration);
        pm.registerEvents(chatListener, this);
    }

    private void registerCommands() {
        commandManager.registerCommands();
    }

    private void scheduleTasks() {
        // Schedule cleanup task (runs every minute)
        getServer().getScheduler().runTaskTimer(
                this,
                new CleanupTask(this, chatListener),
                1200L, // 1 minute delay 
                1200L  // 1 minute interval
        );
    }

    private void setupIntegrations() {
        PluginManager pm = Bukkit.getPluginManager();

        // SRV
        if (pm.getPlugin("DiscordSRV") != null) {
            loggerUtils.info("DiscordSRV detected, initializing Discord integration...");
            setupDiscordIntegration();
        }
    }

    private void setupDiscordIntegration() {
    }

    @Override
    public void onDisable() {
        // Shutdown managers and release resources
        if (dataManager != null) {
            dataManager.shutdown();
        }

        // Close plugin messenger
        if (pluginMessenger != null) {
            pluginMessenger.shutdown();
        }

        // Close message utils
        if (messageUtils != null) {
            messageUtils.close();
        }

        // Close Adventure API
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        // Cancel all tasks
        getServer().getScheduler().cancelTasks(this);

        // Log shutdown
        loggerUtils.logShutdown();
    }

    // Getters
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

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LoggerUtils getLoggerUtils() {
        return loggerUtils;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
}
