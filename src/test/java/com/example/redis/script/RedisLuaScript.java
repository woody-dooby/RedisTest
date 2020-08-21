package com.example.redis.script;

import com.example.redis.config.RedisTransactionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
import java.util.Collections;

@SpringBootTest
@Import(RedisTransactionConfig.class)
public class RedisLuaScript {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void scirptTest(){
        RedisScript<Boolean> redisScript = RedisScript.of(new ClassPathResource("/resources/checkandset.lua"),Boolean.class);
        Object Boolean= redisTemplate.execute(redisScript, Collections.singletonList("key"), Arrays.asList("TEST","VALUE"));
        System.out.println(Boolean);
    }
}
