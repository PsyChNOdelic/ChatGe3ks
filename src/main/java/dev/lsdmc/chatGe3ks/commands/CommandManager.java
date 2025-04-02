package dev.lsdmc.chatGe3ks.commands;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.HashMap;
import java.util.Map;

/**
 * Central manager for all plugin commands
 */
public class CommandManager {

    private final ChatGe3ks plugin;
    private final Map<String, CommandExecutor> commands = new HashMap<>();
    private final LoggerUtils logger;

    public CommandManager(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLoggerUtils();
    }

    /**
     * Registers all plugin commands
     */
    public void registerCommands() {
       
        WelcomeMsgCommand welcomeMsgCommand = new WelcomeMsgCommand(plugin);

        
        registerCommand("welcomemsg", welcomeMsgCommand);

        
    }

    /**
     * Registers a command with both the manager and Bukkit
     *
     * @param commandName The command name from plugin.yml
     * @param executor The command executor
     */
    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);

        if (command == null) {
            logger.severe("Failed to register command '" + commandName + "': not defined in plugin.yml");
            return;
        }

        command.setExecutor(executor);

        // If the executor also implements TabCompleter, register it as tab completer
        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }

        commands.put(commandName, executor);
        logger.debug("Registered command: " + commandName);
    }

    /**
     * Gets a command executor by name
     *
     * @param commandName The command name
     * @return The command executor or null if not found
     */
    public CommandExecutor getCommand(String commandName) {
        return commands.get(commandName);
    }

    /**
     * Gets all registered commands
     *
     * @return Map of command names to executors
     */
    public Map<String, CommandExecutor> getCommands() {
        return new HashMap<>(commands); // Return a copy to prevent external modification
    }
}
