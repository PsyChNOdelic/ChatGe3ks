package dev.lsdmc.chatGe3ks.messenger;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PluginMessenger implements PluginMessageListener {

    private final ChatGe3ks plugin;
    private final LoggerUtils logger;

    // Map to store message callbacks for response handling
    private final Map<UUID, Consumer<byte[]>> responseCallbacks = new HashMap<>();

    public PluginMessenger(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLoggerUtils();
        setupChannels();
    }

    /**
     * Setup plugin messaging channels
     */
    private void setupChannels() {
        try {
            // Register outgoing channel for BungeeCord-style messaging
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Constants.Channels.BUNGEE);

            // Register incoming channel for receiving responses
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Constants.Channels.BUNGEE, this);

            logger.info("Successfully registered plugin messaging channels");
        } catch (Exception e) {
            logger.error("Failed to register plugin messaging channels", e);
        }
    }

    /**
     * Sends a plugin message on the specified subchannel.
     *
     * @param player     A player to send the message through (required for sending).
     * @param subchannel The subchannel (e.g., "Forward", "Connect", etc.).
     * @param data       The raw data bytes to include.
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(Player player, String subchannel, byte[] data) {
        if (player == null || !player.isOnline()) {
            logger.warning("Cannot send plugin message: player is null or offline");
            return false;
        }

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(stream)) {

            out.writeUTF(subchannel);

            if (data != null) {
                out.writeInt(data.length);
                out.write(data);
            } else {
                out.writeInt(0);
            }

            player.sendPluginMessage(plugin, Constants.Channels.BUNGEE, stream.toByteArray());
            return true;
        } catch (IOException e) {
            logger.error("Error sending plugin message", e);
            return false;
        }
    }

    /**
     * Sends a message with expectation of a response and returns a CompletableFuture
     *
     * @param player Player to send through
     * @param subchannel Subchannel to use
     * @param data Data to send
     * @param responseTimeout Timeout in milliseconds
     * @return CompletableFuture that will be completed with the response
     */
    public CompletableFuture<byte[]> sendMessageWithResponse(Player player, String subchannel,
                                                             byte[] data, long responseTimeout) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        UUID requestId = UUID.randomUUID();
        responseCallbacks.put(requestId, response -> future.complete(response));

        // Add the request ID to the data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF(requestId.toString());
            if (data != null) {
                dos.write(data);
            }

            boolean sent = sendMessage(player, subchannel, baos.toByteArray());
            if (!sent) {
                future.completeExceptionally(new IOException("Failed to send message"));
                responseCallbacks.remove(requestId);
            }

            // Set up timeout
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if (!future.isDone()) {
                    future.completeExceptionally(
                            new RuntimeException("Request timed out after " + responseTimeout + "ms"));
                    responseCallbacks.remove(requestId);
                }
            }, responseTimeout / 50); // Convert ms to ticks

        } catch (IOException e) {
            future.completeExceptionally(e);
            responseCallbacks.remove(requestId);
        }

        return future;
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
        if (!channel.equals(Constants.Channels.BUNGEE)) {
            return;
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(message);
             DataInputStream in = new DataInputStream(stream)) {

            String subchannel = in.readUTF();
            int length = in.readInt();
            byte[] data = new byte[length];
            in.readFully(data);

            handleIncomingMessage(subchannel, data, player);

        } catch (IOException e) {
            logger.error("Error reading plugin message", e);
        }
    }

    /**
     * Handle incoming messages based on subchannel
     *
     * @param subchannel Subchannel the message was sent on
     * @param data Message data
     * @param player Player who received the message
     */
    private void handleIncomingMessage(String subchannel, byte[] data, Player player) {
        logger.debug("Received message on subchannel: " + subchannel);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data);
             DataInputStream in = new DataInputStream(stream)) {

            // Check if this is a response to a previous request
            if (subchannel.equals("Response")) {
                String requestIdStr = in.readUTF();
                UUID requestId = UUID.fromString(requestIdStr);

                Consumer<byte[]> callback = responseCallbacks.remove(requestId);
                if (callback != null) {
                    // Read remaining data
                    byte[] responseData = new byte[data.length - (requestIdStr.length() + 2)]; // UTF length bytes
                    in.readFully(responseData);

                    // Execute callback on main thread
                    byte[] finalResponseData = responseData;
                    plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(finalResponseData));
                }
                return;
            }

            // Handle other subchannels
            switch (subchannel) {
                case "Forward":
                    handleForwardMessage(in);
                    break;

                case "PlayerCount":
                    handlePlayerCountMessage(in);
                    break;

                // Add more subchannel handlers as needed

                default:
                    logger.debug("Received message on unhandled subchannel: " + subchannel);
                    break;
            }

        } catch (Exception e) {
            logger.error("Error processing plugin message", e);
        }
    }

    /**
     * Handle Forward subchannel messages
     */
    private void handleForwardMessage(DataInputStream in) throws IOException {
        String server = in.readUTF();
        String channel = in.readUTF();
        short dataLength = in.readShort();
        byte[] forwardedData = new byte[dataLength];
        in.readFully(forwardedData);

        logger.debug("Received forwarded message from server: " + server +
                ", channel: " + channel);

        // Process the forwarded data here if needed
    }

    /**
     * Handle PlayerCount subchannel messages
     */
    private void handlePlayerCountMessage(DataInputStream in) throws IOException {
        String server = in.readUTF();
        int playerCount = in.readInt();

        logger.debug("Server " + server + " has " + playerCount + " players");

        // Process the player count information if needed
    }

    /**
     * Cleanup resources when plugin is disabled
     */
    public void shutdown() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        responseCallbacks.clear();
    }
}