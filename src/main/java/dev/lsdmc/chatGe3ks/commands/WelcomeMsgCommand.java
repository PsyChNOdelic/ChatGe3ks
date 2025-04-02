package dev.lsdmc.chatGe3ks.commands;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.welcome.WelcomeMessagesManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WelcomeMsgCommand implements CommandExecutor {

    private final ChatGe3ks plugin;

    public WelcomeMsgCommand(ChatGe3ks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        WelcomeMessagesManager manager = plugin.getWelcomeMessagesManager();

        if (args.length == 0) {
            sender.sendMessage("Usage: /welcomemsg <list|add|remove|reload>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "list":
                sender.sendMessage("=== Welcome Messages ===");
                int index = 1;
                for (String message : manager.getMessages()) {
                    sender.sendMessage(index + ". " + message);
                    index++;
                }
                break;

            case "add":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /welcomemsg add <message>");
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String newMessage = sb.toString().trim();
                manager.addMessage(newMessage);
                sender.sendMessage("Welcome message added: " + newMessage);
                break;

            case "remove":
                if (args.length != 2) {
                    sender.sendMessage("Usage: /welcomemsg remove <index>");
                    return true;
                }
                try {
                    int removeIndex = Integer.parseInt(args[1]) - 1;
                    if (manager.removeMessage(removeIndex)) {
                        sender.sendMessage("Removed welcome message at index " + (removeIndex + 1));
                    } else {
                        sender.sendMessage("Invalid index.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Please provide a valid number for the index.");
                }
                break;

            case "reload":
                manager.loadMessages();
                sender.sendMessage("Welcome messages reloaded.");
                break;

            default:
                sender.sendMessage("Unknown subcommand. Usage: /welcomemsg <list|add|remove|reload>");
                break;
        }
        return true;
    }
}
