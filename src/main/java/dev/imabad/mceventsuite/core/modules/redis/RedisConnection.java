package dev.imabad.mceventsuite.core.modules.redis;

import dev.imabad.mceventsuite.core.EventCore;
import dev.imabad.mceventsuite.core.api.database.DatabaseProvider;
import dev.imabad.mceventsuite.core.api.database.DatabaseType;
import dev.imabad.mceventsuite.core.config.database.RedisConfig;
import dev.imabad.mceventsuite.core.modules.redis.events.RedisConnectionEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnection extends DatabaseProvider {

    private RedisConfig config;
    private JedisPool connection;
    private Jedis subscriber;
    private Thread redisSubscriberThread;

    public RedisConnection(RedisConfig config) {
        this.config = config;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MEMORY;
    }

    @Override
    public String getName() {
        return "redis";
    }

    @Override
    public void connect() {
        subscriber = new Jedis(config.getHostname(), config.getPort());
        subscriber.connect();
        if(config.getPassword().length() > 0){
            connection = new JedisPool(new JedisPoolConfig(), config.getHostname(), config.getPort(),2000, config.getPassword());
            subscriber.auth(config.getPassword());
        } else {
            connection = new JedisPool(new JedisPoolConfig(), config.getHostname(), config.getPort());
        }
        this.redisSubscriberThread = new Thread(new RedisSubscriberThread(subscriber, config));
        this.redisSubscriberThread.start();
        EventCore.getInstance().getEventRegistry().handleEvent(new RedisConnectionEvent());
    }

    @Override
    public void disconnect() {
        connection.close();
        subscriber.close();
    }

    public Jedis getConnection() {
        return this.connection.getResource();
    }
}
