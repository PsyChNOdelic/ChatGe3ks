package dev.lsdmc.chatGe3ks.listeners;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.data.DataManager;
import dev.lsdmc.chatGe3ks.event.NewPlayerWelcomeEvent;
import dev.lsdmc.chatGe3ks.welcome.WelcomeMessagesManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final ChatGe3ks plugin;
    private final DataManager dataManager;
    private final WelcomeMessagesManager welcomeMessagesManager;

    public JoinListener(ChatGe3ks plugin, DataManager dataManager, WelcomeMessagesManager welcomeMessagesManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.welcomeMessagesManager = welcomeMessagesManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if this is the player's first join across the network using Redis
        if (dataManager.isFirstJoin(player.getUniqueId())) {
            // Fire a custom welcome event so other plugins can hook in
            NewPlayerWelcomeEvent welcomeEvent = new NewPlayerWelcomeEvent(player);
            plugin.getServer().getPluginManager().callEvent(welcomeEvent);
            if (welcomeEvent.isCancelled()) {
                return; // Stop further processing if another plugin cancels the event
            }

            // Retrieve a random welcome message and replace placeholder with player's name
            String message = welcomeMessagesManager.getRandomMessage().replace("{player}", player.getName());
            player.sendMessage(message);

            // Register the player in the ChatListener welcome window for reward tracking
            plugin.getChatListener().registerNewJoin(player);
        }
    }
}
