package dev.lsdmc.chatGe3ks.welcome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class WelcomeMessagesManager {

    private final ChatGe3ks plugin;
    private final File messagesFile;
    private List<String> messages;
    private final Gson gson;
    private final ThreadLocalRandom random;
    private final LoggerUtils logger;

    public WelcomeMessagesManager(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.random = ThreadLocalRandom.current();
        this.messagesFile = new File(plugin.getDataFolder(), Constants.Files.WELCOME_MESSAGES_FILE);
        this.messages = new ArrayList<>();
        this.logger = plugin.getLoggerUtils();
    }

    /**
     * Loads welcome messages from welcome_messages.json.
     * If the file doesn't exist, creates it with default messages.
     */
    public void loadMessages() {
        if (!messagesFile.exists()) {
            createDefaultMessagesFile();
        } else {
            loadExistingMessagesFile();
        }
    }

    private void createDefaultMessagesFile() {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                logger.warning("Failed to create plugin directory");
            }
            messages = getDefaultMessages();
            saveMessages();
            logger.info("Created default welcome messages file with " + messages.size() + " messages");
        } catch (Exception e) {
            logger.error("Failed to create default welcome messages file", e);
            messages = getDefaultMessages(); // Fallback to in-memory defaults
        }
    }

    private void loadExistingMessagesFile() {
        try (Reader reader = new InputStreamReader(new FileInputStream(messagesFile), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> loadedMessages = gson.fromJson(reader, listType);

            if (loadedMessages == null || loadedMessages.isEmpty()) {
                logger.warning("Loaded welcome messages file was empty or invalid, using defaults");
                messages = getDefaultMessages();
                saveMessages(); // Overwrite the invalid file
            } else {
                messages = loadedMessages;
                logger.info("Loaded " + messages.size() + " welcome messages");
            }
        } catch (IOException e) {
            logger.error("Failed to load welcome messages", e);
            messages = getDefaultMessages(); // Fallback to in-memory defaults
        }
    }

    /**
     * Saves the current welcome messages to welcome_messages.json.
     * @return true if save was successful, false otherwise
     */
    public boolean saveMessages() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(messagesFile), StandardCharsets.UTF_8)) {
            gson.toJson(messages, writer);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save welcome messages", e);
            return false;
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
        return messages.get(random.nextInt(messages.size()));
    }

    /**
     * Adds a new welcome message.
     *
     * @param message The message to add.
     * @return true if message was added and saved successfully
     */
    public boolean addMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        messages.add(message.trim());
        return saveMessages();
    }

    /**
     * Removes a welcome message at the specified index.
     *
     * @param index The index (0-based) of the message to remove.
     * @return true if removal was successful.
     */
    public boolean removeMessage(int index) {
        if (index < 0 || index >= messages.size()) {
            return false;
        }

        messages.remove(index);
        return saveMessages();
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
        defaults.add("Welcome to the community, {player}!");
        defaults.add("A wild {player} has appeared!");
        return defaults;
    }

    /**
     * Returns all loaded welcome messages.
     *
     * @return List of welcome messages.
     */
    public List<String> getMessages() {
        return new ArrayList<>(messages); // Return a copy to prevent external modification
    }
}