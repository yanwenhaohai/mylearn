package com.example.myself.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    public static final String REDIS_IP_ADDRESS = "10.57.16.186";
    public static final int REDIS_PORT = 6379;
    public static final String REDIS_PASSWORD = "Td@123456";
    public static JedisPool jedisPool = new JedisPool(getJedisPoolConfig(), REDIS_IP_ADDRESS, REDIS_PORT);

    private static JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        return jedisPoolConfig;
    }

    public static Jedis getJedis() throws Exception {
        if (null != jedisPool) {
            Jedis jedis = jedisPool.getResource();
            jedis.auth(REDIS_PASSWORD);
            return jedis;
        }
        throw new Exception("Jedis pool is not available");
    }
}
