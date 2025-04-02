package dev.lsdmc.chatGe3ks.rewards;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class RewardsManager {

    private final ChatGe3ks plugin;
    private final File rewardsFile;
    private List<Reward> rewards;
    private final Gson gson;
    private final ThreadLocalRandom random;
    private final LoggerUtils logger;

    public RewardsManager(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.random = ThreadLocalRandom.current();
        this.rewardsFile = new File(plugin.getDataFolder(), Constants.Files.REWARDS_FILE);
        this.rewards = new ArrayList<>();
        this.logger = plugin.getLoggerUtils();
    }

    /**
     * Loads rewards from rewards.json.
     * If the file doesn't exist, creates it with default rewards.
     */
    public void loadRewards() {
        if (!rewardsFile.exists()) {
            createDefaultRewardsFile();
        } else {
            loadExistingRewardsFile();
        }
    }

    private void createDefaultRewardsFile() {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                logger.warning("Failed to create plugin directory");
            }
            rewards = getDefaultRewards();
            saveRewards();
            logger.info("Created default rewards file with " + rewards.size() + " rewards.");
        } catch (Exception e) {
            logger.error("Failed to create default rewards file", e);
            rewards = getDefaultRewards(); // Fallback to in-memory defaults
        }
    }

    private void loadExistingRewardsFile() {
        try (Reader reader = new InputStreamReader(new FileInputStream(rewardsFile), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Reward>>() {}.getType();
            List<Reward> loadedRewards = gson.fromJson(reader, listType);

            if (loadedRewards == null || loadedRewards.isEmpty()) {
                logger.warning("Loaded rewards file was empty or invalid, using defaults");
                rewards = getDefaultRewards();
                saveRewards(); // Overwrite the invalid file
            } else {
                rewards = loadedRewards;
                validateRewards();
                logger.info("Loaded " + rewards.size() + " rewards");
            }
        } catch (IOException e) {
            logger.error("Failed to load rewards", e);
            rewards = getDefaultRewards(); // Fallback to in-memory defaults
        }
    }

    /**
     * Validates all rewards to ensure they are valid (e.g., items exist)
     */
    private void validateRewards() {
        List<Reward> invalidRewards = new ArrayList<>();

        for (Reward reward : rewards) {
            if (reward.getType().equalsIgnoreCase("item")) {
                try {
                    Material.valueOf(reward.getValue().toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid material in reward: " + reward.getValue());
                    invalidRewards.add(reward);
                }
            }
        }

        if (!invalidRewards.isEmpty()) {
            rewards.removeAll(invalidRewards);
            logger.warning("Removed " + invalidRewards.size() + " invalid rewards");
            saveRewards(); // Save the fixed rewards
        }
    }

    /**
     * Saves the rewards to rewards.json.
     * @return true if save was successful, false otherwise
     */
    public boolean saveRewards() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(rewardsFile), StandardCharsets.UTF_8)) {
            gson.toJson(rewards, writer);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save rewards", e);
            return false;
        }
    }

    /**
     * Gives a random reward to the specified player based on chance weights.
     *
     * @param welcomer The player receiving the reward.
     * @return true if a reward was given successfully
     */
    public boolean giveRandomReward(Player welcomer) {
        if (rewards.isEmpty()) {
            logger.warning("No rewards defined, cannot give reward to " + welcomer.getName());
            return false;
        }

        if (!welcomer.isOnline()) {
            return false;
        }

        double totalChance = rewards.stream().mapToDouble(Reward::getChance).sum();
        double rand = random.nextDouble() * totalChance;
        double cumulative = 0;

        for (Reward reward : rewards) {
            cumulative += reward.getChance();
            if (rand <= cumulative) {
                return reward.giveReward(welcomer, plugin);
            }
        }

        // Should never reach here if rewards exist and chances are positive
        logger.warning("Failed to select a reward for " + welcomer.getName());
        return false;
    }

    /**
     * Adds a new reward.
     *
     * @param reward The reward to add
     * @return true if the reward was added successfully
     */
    public boolean addReward(Reward reward) {
        if (reward == null) {
            return false;
        }

        // Validate item rewards
        if (reward.getType().equalsIgnoreCase("item")) {
            try {
                Material.valueOf(reward.getValue().toUpperCase());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        rewards.add(reward);
        return saveRewards();
    }

    /**
     * Removes a reward at the specified index.
     *
     * @param index The index to remove
     * @return true if the removal was successful
     */
    public boolean removeReward(int index) {
        if (index < 0 || index >= rewards.size()) {
            return false;
        }

        rewards.remove(index);
        return saveRewards();
    }

    /**
     * Returns a list of default rewards.
     *
     * @return the default rewards.
     */
    private List<Reward> getDefaultRewards() {
        List<Reward> defaults = new ArrayList<>();
        // Default rewards with different chances
        defaults.add(new Reward("item", "DIAMOND", 1, 10.0));
        defaults.add(new Reward("item", "IRON_INGOT", 5, 30.0));
        defaults.add(new Reward("item", "GOLD_INGOT", 3, 20.0));
        defaults.add(new Reward("command", "give {player} minecraft:experience_bottle 5", 0, 40.0));
        return defaults;
    }

    /**
     * Gets all available rewards.
     *
     * @return List of rewards
     */
    public List<Reward> getRewards() {
        return new ArrayList<>(rewards); // Return a copy to prevent external modification
    }

    /**
     * Represents an individual reward.
     * Can be an "item" reward or a "command" reward.
     */
    public static class Reward {
        private String type;   // "item" or "command"
        private String value;  // Material name or command
        private int amount;    // For items: quantity
        private double chance; // Weight for reward selection

        public Reward(String type, String value, int amount, double chance) {
            this.type = type;
            this.value = value;
            this.amount = amount;
            this.chance = chance;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public int getAmount() {
            return amount;
        }

        public double getChance() {
            return chance;
        }

        /**
         * Gives the reward to the player.
         * For "item" rewards, adds the item to the player's inventory.
         * For "command" rewards, dispatches the command from the console.
         *
         * @param player The player receiving the reward.
         * @param plugin The plugin instance for logging
         * @return true if the reward was given successfully
         */
        public boolean giveReward(Player player, ChatGe3ks plugin) {
            if (!player.isOnline()) {
                return false;
            }

            try {
                if (type.equalsIgnoreCase("item")) {
                    return giveItemReward(player, plugin);
                } else if (type.equalsIgnoreCase("command")) {
                    return giveCommandReward(player, plugin);
                } else {
                    plugin.getLoggerUtils().warning("Unknown reward type: " + type);
                    return false;
                }
            } catch (Exception e) {
                plugin.getLoggerUtils().error("Error giving reward to " + player.getName(), e);
                return false;
            }
        }

        private boolean giveItemReward(Player player, ChatGe3ks plugin) {
            try {
                Material material = Material.valueOf(value.toUpperCase());
                ItemStack itemStack = new ItemStack(material, amount);

                // Check if player has inventory space
                if (player.getInventory().firstEmpty() == -1) {
                    // Inventory full, drop at player's location
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                    plugin.getMessageUtils().sendSuccess(player,
                            "Your inventory was full! Your reward (" +
                                    amount + " " + material.name() + ") was dropped at your feet.");
                } else {
                    player.getInventory().addItem(itemStack);
                    plugin.getMessageUtils().sendSuccess(player,
                            "You received a reward: " + amount + " " +
                                    material.name().toLowerCase().replace("_", " "));
                }
                return true;
            } catch (IllegalArgumentException e) {
                plugin.getMessageUtils().sendError(player, "Reward error: Invalid material " + value);
                plugin.getLoggerUtils().warning("Invalid material in reward: " + value);
                return false;
            }
        }

        private boolean giveCommandReward(Player player, ChatGe3ks plugin) {
            String command = value.replace("{player}", player.getName());
            boolean success = Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(), command);

            if (success) {
                plugin.getMessageUtils().sendSuccess(player, "You received a special reward!");
            } else {
                plugin.getLoggerUtils().warning("Failed to execute command reward: " + command);
                return false;
            }
            return success;
        }
    }
}