package dev.lsdmc.chatGe3ks.welcome;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.lsdmc.chatGe3ks.ChatGe3ks;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WelcomeMessagesManager {

    private final ChatGe3ks plugin;
    private final File messagesFile;
    private List<String> messages;
    private final Gson gson;
    private final Random random;

    public WelcomeMessagesManager(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.random = new Random();
        this.messagesFile = new File(plugin.getDataFolder(), "welcome_messages.json");
        this.messages = new ArrayList<>();
    }

    /**
     * Loads welcome messages from welcome_messages.json.
     * If the file doesn't exist, creates it with default messages.
     */
    public void loadMessages() {
        if (!messagesFile.exists()) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            messages = getDefaultMessages();
            saveMessages();
            plugin.getLogger().info("WelcomeMessagesManager: Created default welcome messages file.");
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(messagesFile), StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<List<String>>() {}.getType();
                messages = gson.fromJson(reader, listType);
                if (messages == null) {
                    messages = new ArrayList<>();
                }
                plugin.getLogger().info("WelcomeMessagesManager: Loaded " + messages.size() + " welcome messages.");
            } catch (IOException e) {
                plugin.getLogger().severe("WelcomeMessagesManager: Failed to load welcome messages: " + e.getMessage());
                messages = new ArrayList<>();
            }
        }
    }

    /**
     * Saves the current welcome messages to welcome_messages.json.
     */
    public void saveMessages() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(messagesFile), StandardCharsets.UTF_8)) {
            gson.toJson(messages, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("WelcomeMessagesManager: Failed to save welcome messages: " + e.getMessage());
        }
    }

    /**
     * Returns a random welcome message.
     *
     * @return A welcome message, or a fallback if none are loaded.
     */
    public String getRandomMessage() {
        if (messages.isEmpty()) {
            return "Welcome to the server!";
        }
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }

    /**
     * Adds a new welcome message.
     *
     * @param message The message to add.
     */
    public void addMessage(String message) {
        messages.add(message);
        saveMessages();
    }

    /**
     * Removes a welcome message at the specified index.
     *
     * @param index The index (0-based) of the message to remove.
     * @return true if removal was successful.
     */
    public boolean removeMessage(int index) {
        if (index >= 0 && index < messages.size()) {
            messages.remove(index);
            saveMessages();
            return true;
        }
        return false;
    }

    /**
     * Returns a list of default welcome messages.
     *
     * @return The default messages.
     */
    private List<String> getDefaultMessages() {
        List<String> defaults = new ArrayList<>();
        defaults.add("Welcome {player} to our server!");
        defaults.add("Hello {player}, enjoy your stay!");
        defaults.add("Greetings {player}! Make yourself at home.");
        return defaults;
    }

    /**
     * Returns all loaded welcome messages.
     *
     * @return List of welcome messages.
     */
    public List<String> getMessages() {
        return messages;
    }
}
