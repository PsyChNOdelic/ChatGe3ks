package dev.lsdmc.chatGe3ks.messenger;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class PluginMessenger implements PluginMessageListener {

    private final ChatGe3ks plugin;

    public PluginMessenger(ChatGe3ks plugin) {
        this.plugin = plugin;
        // Register outgoing channel for BungeeCord-style messaging.
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        // Register incoming channel if you need to listen for messages.
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
    }

    /**
     * Sends a plugin message on the specified subchannel.
     *
     * @param player     A player to send the message through (required for sending).
     * @param subchannel The subchannel (e.g., "Forward", "Connect", etc.).
     * @param data       The raw data bytes to include.
     */
    public void sendMessage(Player player, String subchannel, byte[] data) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(stream)) {

            out.writeUTF(subchannel);
            out.writeInt(data.length);
            out.write(data);
            player.sendPluginMessage(plugin, "BungeeCord", stream.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("PluginMessenger: Error sending plugin message: " + e.getMessage());
        }
    }

    /**
     * This method is called when a plugin message is received on the registered channel.
     *
     * @param channel The channel name.
     * @param player  The player who received the message.
     * @param message The raw message data.
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        try (ByteArrayInputStream stream = new ByteArrayInputStream(message);
             DataInputStream in = new DataInputStream(stream)) {

            String subchannel = in.readUTF();
            int length = in.readInt();
            byte[] data = new byte[length];
            in.readFully(data);

            // Process the subchannel and data as needed.
            plugin.getLogger().info("PluginMessenger: Received message on subchannel " + subchannel);

            // Example: For a "Forward" subchannel, you might process the forwarded data.
            if (subchannel.equals("Forward")) {
                // Handle forwarded data here...
            }
        } catch (IOException e) {
            plugin.getLogger().severe("PluginMessenger: Error reading plugin message: " + e.getMessage());
        }
    }
}
