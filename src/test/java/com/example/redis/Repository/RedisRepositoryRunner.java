package com.example.redis.Repository;

import com.example.redis.config.RedisRepositoryConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@Import(RedisRepositoryConfig.class)
public class RedisRepositoryRunner {

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void basicCrudOperation(){
        RedisEntity entity = new RedisEntity("codingcheol","seoul");
        redisRepository.save(entity);

        RedisEntity entity1  = redisRepository.findById(entity.getId()).orElse(new RedisEntity("error","error"));

        Assertions.assertEquals("codingcheol",entity1.getName());
        Assertions.assertEquals(1,redisRepository.count());

        redisRepository.delete(entity);

        Assertions.assertEquals(0,redisRepository.count());
    }

}
