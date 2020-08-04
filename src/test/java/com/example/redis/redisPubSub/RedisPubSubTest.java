package com.example.redis.redisPubSub;

import com.example.redis.config.RedisPubSubConfig;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.File;
import java.time.Duration;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
@Import(RedisPubSubConfig.class)
public class RedisPubSubTest {


    private static String expireKey = "customkey";
    @Autowired
    RedisMessageListenerContainer container;

    @Autowired
    @Qualifier(value = "CustomTemplate")
    RedisTemplate<String, String> template;

    @Test
    void pubSubTest() throws InterruptedException {
        String channel = "TEST";

        //subscribe
        container.addMessageListener(
                //onMessage 실제 구현시 해당 부분을 구현해보자..
                (message, channelNm)->{
                    log.info("[{}] Message : {}",new String(channelNm),new String(message.getBody()));
                },new ChannelTopic(channel));

        //publish
        IntStream.range(0,20).forEach(index->{
            template.convertAndSend(channel,"PUB_SUB_TEST "+index);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
    @Test
    void keyExpireTest(){

        container.addMessageListener(
                (message,channelNm)->{
                    String key = new String(message.getBody());
                    String reform=  key.substring(key.indexOf(File.separatorChar));
                    String body = template.opsForValue().get(reform);
                    Boolean bool = template.hasKey(key);
                    Boolean bool2 = template.hasKey(reform);
                    log.info("expired key: {} / body : {}", reform , body);
                    log.info("expired bool: {}", bool);
                    log.info("expired bool2: {}", bool2);
                    template.delete(key);
                },
                new PatternTopic("__keyevent@*__:expired"));
        container.setErrorHandler(throwable -> {
            log.error(throwable.getMessage());
        });

        //publish
        IntStream.range(0,20).forEach(index->{
            template.opsForValue().set("test","testetst"+index);
            template.opsForValue().set(expireKey + File.separatorChar+"test", StringUtil.EMPTY_STRING);
            String data = template.opsForValue().get("test");
            log.info(data);
            template.expire(expireKey + File.separatorChar+"test", Duration.ofMillis(1000L));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
