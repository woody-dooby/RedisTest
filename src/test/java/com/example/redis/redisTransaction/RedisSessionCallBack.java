package com.example.redis.redisTransaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@Slf4j
@SpringBootTest
@Import(com.example.redis.config.RedisConfig.class)
public class RedisSessionCallBack {

    StringRedisTemplate redisTemplate;

    @BeforeEach
    void init(@Autowired RedisConnectionFactory factory){
        this.redisTemplate = new StringRedisTemplate(factory);
    }

    @Test
    void sessionCallBack(){

         // 수행된 개수만을 리턴하여 준다.
         List<Object> txResult = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                for(int i =0 ; i < 10000 ; i++){
                    redisOperations.opsForValue().set("key "+i,"value");
                }

                return redisOperations.exec();
            }
        });

        Assertions.assertEquals(10000,txResult.size());

        for(int i =0 ; i < 10000 ; i++) {
            Assertions.assertNotNull(redisTemplate.opsForValue().get("key "+i));
//            log.info("RedisTemplate key:  {} value: {}",i,redisTemplate.opsForValue().get("key "+i) );

        }

    }

}
