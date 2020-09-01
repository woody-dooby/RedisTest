package com.example.redis.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@TestConfiguration
@EnableRedisRepositories
@Import(RedisConfig.class)
public class RedisRepositoryConfig {

    @Bean
    public RedisTemplate setTemplate(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }
}
