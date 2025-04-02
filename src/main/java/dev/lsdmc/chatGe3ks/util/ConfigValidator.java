package dev.lsdmc.chatGe3ks.util;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates plugin configuration values and provides safe defaults
 */
public class ConfigValidator {

    private final ChatGe3ks plugin;
    private final Map<String, String> validationErrors = new HashMap<>();
    private final LoggerUtils logger;

    public ConfigValidator(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLoggerUtils();
    }

    /**
     * Validates the entire plugin configuration
     * @return true if configuration is valid, false if corrections were needed
     */
    public boolean validateConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean isValid = true;

        // Clear previous validation errors
        validationErrors.clear();

        // Validate welcome window duration
        if (!validateInt(Constants.Config.WELCOME_WINDOW, 5, 3600, 60)) {
            isValid = false;
        }

        // Validate Redis connection settings
        if (!validateRedisConfig(config)) {
            isValid = false;
        }

        // Log validation errors
        if (!isValid) {
            logger.warning("Configuration validation found " + validationErrors.size() + " issues:");
            for (Map.Entry<String, String> error : validationErrors.entrySet()) {
                logger.warning(" - " + error.getKey() + ": " + error.getValue());
            }
        }

        return isValid;
    }

    /**
     * Validates Redis configuration section
     * @param config Main configuration
     * @return true if valid
     */
    private boolean validateRedisConfig(FileConfiguration config) {
        boolean isValid = true;

        // Check if Redis section exists
        if (!config.isConfigurationSection("redis")) {
            validationErrors.put("redis", "Redis section is missing");
            return false;
        }

        // Validate host
        if (!validateString("redis.host", 1, 255, "localhost")) {
            isValid = false;
        }

        // Validate port
        if (!validateInt("redis.port", 1, 65535, 6379)) {
            isValid = false;
        }

        // Validate timeout
        if (!validateInt("redis.timeout", 500, 30000, 2000)) {
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validates an integer configuration value within a range
     * @param path Configuration path
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @param defaultValue Default value to use if invalid
     * @return true if value was valid
     */
    private boolean validateInt(String path, int min, int max, int defaultValue) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path, "Missing value, set to default: " + defaultValue);
            return false;
        }

        if (!plugin.getConfig().isInt(path)) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path, "Not an integer, set to default: " + defaultValue);
            return false;
        }

        int value = plugin.getConfig().getInt(path);
        if (value < min || value > max) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path,
                    "Value " + value + " out of range (" + min + "-" + max + "), set to default: " + defaultValue);
            return false;
        }

        return true;
    }

    /**
     * Validates a string configuration value
     * @param path Configuration path
     * @param minLength Minimum allowed length
     * @param maxLength Maximum allowed length
     * @param defaultValue Default value to use if invalid
     * @return true if value was valid
     */
    private boolean validateString(String path, int minLength, int maxLength, String defaultValue) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path, "Missing value, set to default: " + defaultValue);
            return false;
        }

        if (!plugin.getConfig().isString(path)) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path, "Not a string, set to default: " + defaultValue);
            return false;
        }

        String value = plugin.getConfig().getString(path, "");
        if (value.length() < minLength || value.length() > maxLength) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path,
                    "String length " + value.length() + " out of range (" + minLength + "-" + maxLength +
                            "), set to default: " + defaultValue);
            return false;
        }

        return true;
    }

    /**
     * Validates if a value exists in an enum (e.g., for configuration options)
     * @param path Configuration path
     * @param validValues Array of valid values
     * @param defaultValue Default value to use if invalid
     * @return true if value was valid
     */
    private boolean validateEnum(String path, String[] validValues, String defaultValue) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path, "Missing value, set to default: " + defaultValue);
            return false;
        }

        if (!plugin.getConfig().isString(path)) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path, "Not a string, set to default: " + defaultValue);
            return false;
        }

        String value = plugin.getConfig().getString(path, "");
        if (Arrays.stream(validValues).noneMatch(v -> v.equalsIgnoreCase(value))) {
            plugin.getConfig().set(path, defaultValue);
            validationErrors.put(path,
                    "Invalid value: '" + value + "', must be one of: " +
                            String.join(", ", validValues) + ". Set to default: " + defaultValue);
            return false;
        }

        return true;
    }

    /**
     * Gets map of validation errors from the last validation
     * @return Map of error paths to error messages
     */
    public Map<String, String> getValidationErrors() {
        return new HashMap<>(validationErrors);
    }
}