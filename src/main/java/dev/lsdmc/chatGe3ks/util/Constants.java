package dev.lsdmc.chatGe3ks.util;

/**
 * Central location for all plugin constants
 */
public final class Constants {
    // Prevent instantiation
    private Constants() {}

    // Plugin information
    public static final class Plugin {
        public static final String NAME = "ChatGe3ks";
        public static final String VERSION = "1.0.0";
        public static final String AUTHOR = "Nenf";
    }

    // File paths
    public static final class Files {
        public static final String WELCOME_MESSAGES_FILE = "welcome_messages.json";
        public static final String REWARDS_FILE = "rewards.json";
    }

    // Redis constants
    public static final class Redis {
        public static final String KEY_PREFIX = "chatgeeks:";
        public static final String FIRSTJOIN_PREFIX = KEY_PREFIX + "firstjoin:";
        public static final int DEFAULT_PORT = 6379;
        public static final int DEFAULT_TIMEOUT = 2000;
        public static final int MAX_ATTEMPTS = 3;
        public static final long RETRY_DELAY = 500;
    }

    // Config keys
    public static final class Config {
        public static final String REDIS_HOST = "redis.host";
        public static final String REDIS_PORT = "redis.port";
        public static final String REDIS_PASSWORD = "redis.password";
        public static final String REDIS_TIMEOUT = "redis.timeout";
        public static final String WELCOME_WINDOW = "welcome-window";
    }

    // Permission nodes
    public static final class Permissions {
        public static final String COMMAND_BASE = "chatgeeks.command";
        public static final String WELCOME_BASE = COMMAND_BASE + ".welcomemsg";
        public static final String WELCOME_LIST = WELCOME_BASE + ".list";
        public static final String WELCOME_ADD = WELCOME_BASE + ".add";
        public static final String WELCOME_REMOVE = WELCOME_BASE + ".remove";
        public static final String WELCOME_RELOAD = WELCOME_BASE + ".reload";
    }

    // Chat formatting
    public static final class Chat {
        public static final String PREFIX = "§8[§6ChatGe3ks§8] §r";
        public static final String ERROR = PREFIX + "§c";
        public static final String SUCCESS = PREFIX + "§a";
        public static final String INFO = PREFIX + "§e";
        public static final String HELP = PREFIX + "§7";
    }

    // Time constants
    public static final class Time {
        public static final int TICKS_PER_SECOND = 20;
        public static final long DEFAULT_WELCOME_WINDOW_SECONDS = 60;
    }

    // Plugin messaging channels
    public static final class Channels {
        public static final String BUNGEE = "BungeeCord";
        public static final String WELCOME = "chatgeeks:welcome";
    }
}