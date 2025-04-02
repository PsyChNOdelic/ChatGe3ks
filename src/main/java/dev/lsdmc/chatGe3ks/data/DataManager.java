package dev.lsdmc.chatGe3ks.data;

import dev.lsdmc.chatGe3ks.ChatGe3ks;
import dev.lsdmc.chatGe3ks.util.Constants;
import dev.lsdmc.chatGe3ks.util.LoggerUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DataManager {

    private final ChatGe3ks plugin;
    private JedisPool jedisPool;
    private boolean redisAvailable = false;

    // Local cache for first join data in case Redis is unavailable
    private final Map<UUID, Long> firstJoinCache = new ConcurrentHashMap<>();
    private final LoggerUtils logger;

    public DataManager(ChatGe3ks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLoggerUtils();
    }

    /**
     * Initializes the Redis connection using configuration values from config.yml.
     *
     * @return true if initialization was successful
     */
    public boolean init() {
        try {
            String host = plugin.getConfig().getString("redis.host", "localhost");
            int port = plugin.getConfig().getInt("redis.port", 6379);
            String password = plugin.getConfig().getString("redis.password", "");
            int timeout = plugin.getConfig().getInt("redis.timeout", 2000);

            // Configure connection pool
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setTestOnBorrow(true);

            // Initialize the JedisPool
            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout);
            }

            // Test the connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping(); // This will throw an exception if connection fails
                redisAvailable = true;
                logger.info("Successfully connected to Redis at " + host + ":" + port);
                return true;
            }
        } catch (JedisConnectionException e) {
            redisAvailable = false;
            logger.warning("Failed to connect to Redis: " + e.getMessage());
            logger.warning("Falling back to local cache for first join detection");
            return false;
        } catch (Exception e) {
            redisAvailable = false;
            logger.error("Error initializing Redis connection", e);
            logger.warning("Falling back to local cache for first join detection");
            return false;
        }
    }

    /**
     * Checks if the player with the given UUID is joining for the first time.
     * Uses Redis if available, otherwise falls back to local cache.
     *
     * @param uuid The player's UUID.
     * @return true if this is the first join, false otherwise.
     */
    public boolean isFirstJoin(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        if (!redisAvailable) {
            return isFirstJoinLocalCache(uuid);
        }

        int attempts = 0;
        int maxAttempts = 3;
        while (attempts < maxAttempts) {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = "chatgeeks:firstjoin:" + uuid.toString();

                // SETNX returns 1 if the key was set (i.e., player is new)
                long result = jedis.setnx(key, String.valueOf(System.currentTimeMillis()));

                // Cache the result locally as well
                if (result == 1) {
                    firstJoinCache.put(uuid, System.currentTimeMillis());
                }

                return result == 1;
            } catch (JedisConnectionException e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    logger.warning("Redis connection failed after " + maxAttempts +
                            " attempts, falling back to local cache");
                    redisAvailable = false;
                    return isFirstJoinLocalCache(uuid);
                }

                // Wait before retrying
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return isFirstJoinLocalCache(uuid);
                }
            } catch (Exception e) {
                logger.error("Error checking first join for " + uuid, e);
                return isFirstJoinLocalCache(uuid);
            }
        }

        // Should never reach here, but just in case
        return isFirstJoinLocalCache(uuid);
    }

    /**
     * Fallback method to check first join using local cache
     *
     * @param uuid The player's UUID
     * @return true if this is the first join according to local cache
     */
    private boolean isFirstJoinLocalCache(UUID uuid) {
        if (firstJoinCache.containsKey(uuid)) {
            return false;
        }

        firstJoinCache.put(uuid, System.currentTimeMillis());
        return true;
    }

    /**
     * Shuts down the Redis connection pool.
     */
    public void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("Redis connection closed");
        }
    }

    /**
     * Checks if Redis is available
     *
     * @return true if Redis is connected and available
     */
    public boolean isRedisAvailable() {
        if (!redisAvailable || jedisPool == null || jedisPool.isClosed()) {
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
            return true;
        } catch (Exception e) {
            redisAvailable = false;
            return false;
        }
    }

    /**
     * Attempts to reconnect to Redis if the connection was lost
     *
     * @return true if reconnection was successful
     */
    public boolean reconnect() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }

        return init();
    }
}