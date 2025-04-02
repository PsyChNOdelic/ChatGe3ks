package dev.lsdmc.chatGe3ks.data;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class DataManager {

    private final ChatGe3ks plugin;
    private JedisPool jedisPool;

    public DataManager(ChatGe3ks plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the Redis connection using configuration values from config.yml.
     */
    public void init() {
        String host = plugin.getConfig().getString("redis.host", "localhost");
        int port = plugin.getConfig().getInt("redis.port", 6379);
        // Note: JedisPool doesn't support the "user" field directly.
        String password = plugin.getConfig().getString("redis.password", "");
        int timeout = plugin.getConfig().getInt("redis.timeout", 2000); // Timeout in milliseconds, default 2000

        // Create a default pool configuration
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

        // Initialize the JedisPool with pool config, host, port, timeout, and password.
        jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
        plugin.getLogger().info("DataManager: Connected to Redis at " + host + ":" + port);
    }

    /**
     * Checks if the player with the given UUID is joining for the first time using Redis SETNX.
     *
     * @param uuid The player's UUID.
     * @return true if this is the first join, false otherwise.
     */
    public boolean isFirstJoin(UUID uuid) {
        String key = "firstjoin:" + uuid.toString();
        try (Jedis jedis = jedisPool.getResource()) {
            // SETNX returns 1 if the key was set (i.e., player is new)
            long result = jedis.setnx(key, String.valueOf(System.currentTimeMillis()));
            return result == 1;
        } catch (Exception e) {
            plugin.getLogger().severe("DataManager: Error checking first join for " + uuid + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Shuts down the Redis connection pool.
     */
    public void shutdown() {
        if (jedisPool != null) {
            jedisPool.close();
            plugin.getLogger().info("DataManager: Redis connection closed.");
        }
    }
}
