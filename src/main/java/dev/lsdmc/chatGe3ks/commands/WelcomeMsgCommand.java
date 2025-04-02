package dev.lsdmc.chatGe3ks.commands;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import dev.lsdmc.chatGe3ks.util.MessageUtils;
import dev.lsdmc.chatGe3ks.welcome.WelcomeMessagesManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WelcomeMsgCommand implements CommandExecutor, TabCompleter {

    private final ChatGe3ks plugin;
    private final WelcomeMessagesManager manager;
    private final MessageUtils messageUtils;
    private final LoggerUtils logger;

    public WelcomeMsgCommand(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.manager = plugin.getWelcomeMessagesManager();
        this.messageUtils = plugin.getMessageUtils();
        this.logger = plugin.getLoggerUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "list":
                return handleListCommand(sender, args);
            case "add":
                return handleAddCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "help":
                showHelp(sender);
                return true;
            default:
                messageUtils.sendError(sender, "Unknown subcommand. Use /welcomemsg help for usage information.");
                return true;
        }
    }

    private boolean handleListCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, Constants.Permissions.WELCOME_LIST)) {
            messageUtils.sendError(sender, "You don't have permission to list welcome messages.");
            return true;
        }

        List<String> messages = manager.getMessages();
        if (messages.isEmpty()) {
            messageUtils.sendError(sender, "No welcome messages have been defined yet.");
            return true;
        }

        // Handle pagination
        int page = 1;
        int perPage = 5;

        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                messageUtils.sendError(sender, "Invalid page number.");
                return true;
            }
        }

        int totalPages = (int) Math.ceil(messages.size() / (double) perPage);
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, messages.size());

        messageUtils.sendInfo(sender, "=== Welcome Messages (Page " + page + "/" + totalPages + ") ===");
        for (int i = start; i < end; i++) {
            messageUtils.sendInfo(sender, (i + 1) + ". " + messages.get(i));
        }

        if (page < totalPages) {
            messageUtils.sendInfo(sender, "Use /welcomemsg list " + (page + 1) + " for the next page");
        }

        return true;
    }

    private boolean handleAddCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, Constants.Permissions.WELCOME_ADD)) {
            messageUtils.sendError(sender, "You don't have permission to add welcome messages.");
            return true;
        }

        if (args.length < 2) {
            messageUtils.sendError(sender, "Usage: /welcomemsg add <message>");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String newMessage = sb.toString().trim();

        if (newMessage.isEmpty()) {
            messageUtils.sendError(sender, "Message cannot be empty.");
            return true;
        }

        if (manager.addMessage(newMessage)) {
            messageUtils.sendSuccess(sender, "Welcome message added: " + newMessage);
            logger.info(sender.getName() + " added welcome message: " + newMessage);
        } else {
            messageUtils.sendError(sender, "Failed to add welcome message.");
        }

        return true;
    }

    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, Constants.Permissions.WELCOME_REMOVE)) {
            messageUtils.sendError(sender, "You don't have permission to remove welcome messages.");
            return true;
        }

        if (args.length != 2) {
            messageUtils.sendError(sender, "Usage: /welcomemsg remove <index>");
            return true;
        }

        try {
            int removeIndex = Integer.parseInt(args[1]) - 1; // Convert to 0-based index
            if (manager.removeMessage(removeIndex)) {
                messageUtils.sendSuccess(sender, "Removed welcome message at index " + (removeIndex + 1));
                logger.info(sender.getName() + " removed welcome message at index " + (removeIndex + 1));
            } else {
                messageUtils.sendError(sender, "Invalid index. Use /welcomemsg list to see available messages.");
            }
        } catch (NumberFormatException e) {
            messageUtils.sendError(sender, "Please provide a valid number for the index.");
        }

        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, Constants.Permissions.WELCOME_RELOAD)) {
            messageUtils.sendError(sender, "You don't have permission to reload welcome messages.");
            return true;
        }

        manager.loadMessages();
        messageUtils.sendSuccess(sender, "Welcome messages reloaded.");
        logger.info(sender.getName() + " reloaded welcome messages");

        return true;
    }

    private void showHelp(CommandSender sender) {
        messageUtils.sendInfo(sender, "=== Welcome Messages Commands ===");
        messageUtils.sendInfo(sender, "/welcomemsg list [page] - List all welcome messages");
        messageUtils.sendInfo(sender, "/welcomemsg add <message> - Add a new welcome message");
        messageUtils.sendInfo(sender, "/welcomemsg remove <index> - Remove a message by index");
        messageUtils.sendInfo(sender, "/welcomemsg reload - Reload messages from file");
        messageUtils.sendInfo(sender, "=== Placeholders ===");
        messageUtils.sendInfo(sender, "{player} - Player's name");
        messageUtils.sendInfo(sender, "{server} - Server name");
        messageUtils.sendInfo(sender, "{online} - Online player count");
    }

    /**
     * Checks if the sender has the specified permission
     */
    private boolean hasPermission(CommandSender sender, String permission) {
        // Console always has permission
        if (!(sender instanceof Player)) {
            return true;
        }

        // Check for specific permission or admin permission
        return sender.hasPermission(permission) || sender.hasPermission(Constants.Permissions.WELCOME_BASE + ".*");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // First argument - suggest subcommands the player has permission for
            List<String> subcommands = new ArrayList<>();

            if (hasPermission(sender, Constants.Permissions.WELCOME_LIST)) subcommands.add("list");
            if (hasPermission(sender, Constants.Permissions.WELCOME_ADD)) subcommands.add("add");
            if (hasPermission(sender, Constants.Permissions.WELCOME_REMOVE)) subcommands.add("remove");
            if (hasPermission(sender, Constants.Permissions.WELCOME_RELOAD)) subcommands.add("reload");
            subcommands.add("help");

            return filterByStart(subcommands, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") && hasPermission(sender, Constants.Permissions.WELCOME_REMOVE)) {
                // For the remove subcommand, suggest message indices
                int messageCount = manager.getMessages().size();
                return IntStream.rangeClosed(1, messageCount)
                        .mapToObj(String::valueOf)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("list") && hasPermission(sender, Constants.Permissions.WELCOME_LIST)) {
                // For the list command, suggest page numbers
                int totalPages = (int) Math.ceil(manager.getMessages().size() / 5.0);
                return IntStream.rangeClosed(1, totalPages)
                        .mapToObj(String::valueOf)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("add") && hasPermission(sender, Constants.Permissions.WELCOME_ADD)) {
                // For the add command, suggest some placeholders
                return filterByStart(Arrays.asList("{player}", "{server}", "{online}"), args[1]);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Filter a list of strings by the start string (case-insensitive)
     */
    private List<String> filterByStart(List<String> options, String start) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(start.toLowerCase()))
                .collect(Collectors.toList());
    }
}