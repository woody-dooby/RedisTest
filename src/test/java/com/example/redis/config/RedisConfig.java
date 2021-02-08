package com.example.redis.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

@TestConfiguration
public class RedisConfig {

 /*   @Bean
    public LettuceConnectionFactory setFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("127.0.0.1");
        configuration.setPort(6379);
        configuration.setPassword(RedisPassword.of("password2"));
        return new LettuceConnectionFactory(configuration);
    }*/
}
