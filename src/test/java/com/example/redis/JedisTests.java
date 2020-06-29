package com.example.redis;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JedisTests {
    private JedisPool jedisPool;
    private final static String[] JEDIS_PUBSUB_CHANNEL = new String[]{"JEDIS_CHANNEL1","JEDIS_CHANNEL2"};

    @BeforeEach
    void init(){
        setJedisConnectionPool();
    }
    private void setJedisConnectionPool(){
        JedisPoolConfig config  = new JedisPoolConfig();
        jedisPool = new JedisPool(config,"127.0.0.1",6379,1000,"password");
    }
    private Jedis getJedisInstance(){
        if(jedisPool == null){
            setJedisConnectionPool();
        }
        return jedisPool.getResource();
    }

    @Test
    void stringDataSetTest(){
        Jedis jedis = getJedisInstance();
        jedis.set("test","가");
        jedis.set("test2","나");
        jedis.set("test3","다");
        jedis.set("test4","라");

        assertEquals(jedis.get("test"),"가");
        assertEquals(jedis.get("test2"),"나");
        assertEquals(jedis.get("test3"),"다");
        assertEquals(jedis.get("test4"),"라");
    }

    @Test
    void listDataSetTest(){
        Jedis jedis = getJedisInstance();

        String key = String.valueOf(UUID.randomUUID());
        log.info("key ::{}",key);
        jedis.lpush(key ,"가","나","NO","다","라");

        assertEquals(jedis.lpop(key),"라");
        assertEquals(jedis.rpop(key),"가");
        assertEquals(jedis.rpop(key),"나");
        assertEquals(jedis.lpop(key),"다");
    }

    @Test
    void setDataSetTest(){
        Jedis jedis = getJedisInstance();

        String key = String.valueOf(UUID.randomUUID());
        log.info("key ::{}",key);
        jedis.sadd(key,"가","나","NO","다","라");

        Set<String> set = jedis.smembers(key);
        set.forEach(System.out::println);
    }

    @Test
    void hashesDataSetTest(){
        Jedis jedis = getJedisInstance();

        String key = String.valueOf(UUID.randomUUID());
        log.info("key ::{}",key);

        jedis.hset(key,"case1","가");
        jedis.hset(key,"case1","가");
        jedis.hset(key,"case2","나");
        jedis.hset(key,"case3","다");
        jedis.hset(key,"case4","라");
        jedis.hset(key,"case5","마");

        assertEquals(jedis.hget(key,"case1"),"가");
        assertEquals(jedis.hget(key,"case2"),"나");
        assertEquals(jedis.hget(key,"case3"),"다");
        assertEquals(jedis.hget(key,"case4"),"라");
        assertEquals(jedis.hget(key,"case5"),"마");
    }

    @Test
    void sortedSetDataSetTest(){
        Jedis jedis = getJedisInstance();
        String key = String.valueOf(UUID.randomUUID());
        log.info("key ::{}",key);

        jedis.zadd(key,111,"가");
        jedis.zadd(key,222,"나");
        jedis.zadd(key,333,"다");
        jedis.zadd(key,444,"라");
        jedis.zadd(key,555,"마");

        Set<String> set = jedis.zrangeByScore(key,100,400);
        set.forEach(System.out::println);
    }

    @Test
    void pubSubTest() throws InterruptedException {
        //Make subscribe
        ExecutorService service = Executors.newFixedThreadPool(4);
        Jedis subJedis = getJedisInstance();
        JedisPubSub subscriber = getJedisPubSub();
//        IntStream.range(0,4).forEach(index-> service.execute(()->subJedis.subscribe(subscriber,JEDIS_PUBSUB_CHANNEL)));
        service.execute(()->subJedis.subscribe(subscriber,JEDIS_PUBSUB_CHANNEL));

        //push Publish
        Jedis pubJedis = getJedisInstance();
        log.info("publish start");
        Stream.of(JEDIS_PUBSUB_CHANNEL).forEach(channel->{
            pubJedis.publish(channel,"START");
        });

        for (int i = 0; i < 5; i++) {
            Stream.of(JEDIS_PUBSUB_CHANNEL).forEach(channel->{
                String message=UUID.randomUUID().toString();
                pubJedis.publish(channel,message);
            });
            Thread.sleep(1000);
        }

        Stream.of(JEDIS_PUBSUB_CHANNEL).forEach(channel-> {
            pubJedis.publish(channel,"END");
        });
        Thread.sleep(1000);

    }
    private JedisPubSub getJedisPubSub(){
        return new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                log.info("channel : {}",channel);
                log.info("message : {}",message);
                if(message.equals("END")){
                    unsubscribe(channel);
                }
            }
            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                log.info("Client is Subscribed to channel : "+ channel);
                log.info("Client is Subscribed to "+ subscribedChannels + " no. of channels");
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                log.info("Client is Unsubscribed from channel : "+ channel);
                log.info("Client is Subscribed to "+ subscribedChannels + " no. of channels");
            }
        };
    }

}
