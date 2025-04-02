package dev.lsdmc.chatGe3ks.rewards;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import com.google.gson.Gson;
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
import java.util.Random;

public class RewardsManager {

    private final ChatGe3ks plugin;
    private final File rewardsFile;
    private List<Reward> rewards;
    private final Gson gson;
    private final Random random;

    public RewardsManager(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.random = new Random();
        this.rewardsFile = new File(plugin.getDataFolder(), "rewards.json");
        this.rewards = new ArrayList<>();
    }

    /**
     * Loads rewards from rewards.json.
     * If the file doesn't exist, creates it with default rewards.
     */
    public void loadRewards() {
        if (!rewardsFile.exists()) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            rewards = getDefaultRewards();
            saveRewards();
            plugin.getLogger().info("RewardsManager: Created default rewards file.");
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(rewardsFile), StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<Reward>>() {}.getType();
                rewards = gson.fromJson(reader, listType);
                if (rewards == null) {
                    rewards = new ArrayList<>();
                }
                plugin.getLogger().info("RewardsManager: Loaded " + rewards.size() + " rewards.");
            } catch (IOException e) {
                plugin.getLogger().severe("RewardsManager: Failed to load rewards: " + e.getMessage());
                rewards = new ArrayList<>();
            }
        }
    }

    /**
     * Saves the rewards to rewards.json.
     */
    public void saveRewards() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(rewardsFile), StandardCharsets.UTF_8)) {
            gson.toJson(rewards, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("RewardsManager: Failed to save rewards: " + e.getMessage());
        }
    }

    /**
     * Gives a random reward to the specified player based on chance weights.
     *
     * @param welcomer The player receiving the reward.
     */
    public void giveRandomReward(Player welcomer) {
        if (rewards.isEmpty()) {
            plugin.getLogger().warning("RewardsManager: No rewards defined.");
            return;
        }
        double totalChance = rewards.stream().mapToDouble(Reward::getChance).sum();
        double rand = random.nextDouble() * totalChance;
        double cumulative = 0;
        for (Reward reward : rewards) {
            cumulative += reward.getChance();
            if (rand <= cumulative) {
                reward.giveReward(welcomer);
                return;
            }
        }
    }

    /**
     * Returns a list of default rewards.
     *
     * @return the default rewards.
     */
    private List<Reward> getDefaultRewards() {
        List<Reward> defaults = new ArrayList<>();
        // Default reward: Give 1 diamond with 100% chance.
        defaults.add(new Reward("item", "DIAMOND", 1, 100.0));
        // Add more defaults if needed.
        return defaults;
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
         */
        public void giveReward(Player player) {
            if (type.equalsIgnoreCase("item")) {
                try {
                    Material material = Material.valueOf(value.toUpperCase());
                    ItemStack itemStack = new ItemStack(material, amount);
                    player.getInventory().addItem(itemStack);
                    player.sendMessage("You received a reward: " + amount + " " + material.name());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Reward error: Invalid material " + value);
                }
            } else if (type.equalsIgnoreCase("command")) {
                String command = value.replace("{player}", player.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                player.sendMessage("You received a reward: Command executed!");
            } else {
                player.sendMessage("Reward error: Unknown reward type " + type);
            }
        }
    }
}
