package dev.lsdmc.chatGe3ks.util;

import dev.lsdmc.chatGe3ks.ChatGe3ks;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for enhanced logging
 */
public class LoggerUtils {

    private final Logger logger;
    private boolean debugEnabled;

    /**
     * Creates a new logger utility
     *
     * @param plugin The plugin instance
     */
    public LoggerUtils(ChatGe3ks plugin) {
        this.logger = plugin.getLogger();
        this.debugEnabled = plugin.getConfig().getBoolean("debug", false);
    }

    /**
     * Log a debug message (only shown if debug is enabled)
     *
     * @param message The message to log
     */
    public void debug(String message) {
        if (debugEnabled) {
            logger.info("[DEBUG] " + message);
        }
    }

    /**
     * Log an info message
     *
     * @param message The message to log
     */
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Log a warning message
     *
     * @param message The message to log
     */
    public void warning(String message) {
        logger.warning(message);
    }

    /**
     * Log a severe error message
     *
     * @param message The message to log
     */
    public void severe(String message) {
        logger.severe(message);
    }

    /**
     * Log an error message with exception
     *
     * @param message The message to log
     * @param throwable The exception to log
     */
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Log startup information
     */
    public void logStartup() {
        logger.info("===============================");
        logger.info(" ChatGe3ks v" + Constants.Plugin.VERSION);
        logger.info(" Author: " + Constants.Plugin.AUTHOR);
        logger.info("===============================");
    }

    /**
     * Log shutdown information
     */
    public void logShutdown() {
        logger.info("===============================");
        logger.info(" ChatGe3ks is shutting down");
        logger.info("===============================");
    }

    /**
     * Set debug mode
     *
     * @param enabled Whether debug mode is enabled
     */
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    /**
     * Check if debug mode is enabled
     *
     * @return true if debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}