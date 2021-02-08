package com.example.redis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;

@TestConfiguration
@EnableConfigurationProperties(RedisProperties.class)
@RequiredArgsConstructor
public class RedisEmbeddedConfig {

    final RedisProperties properties;
    private RedisServer redisServer;

    @PostConstruct
    public void serverStart(){
        redisServer = new RedisServer(properties.getPort());
        redisServer.start();
    }

    @PreDestroy
    public void serverStop(){
        Optional.ofNullable(redisServer).ifPresent(RedisServer::stop);
    }


    @Bean
    public LettuceConnectionFactory setEmbeddedFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(properties.getHost());
        configuration.setPort(properties.getPort());
        configuration.setPassword(RedisPassword.none());
        return new LettuceConnectionFactory(configuration);
    }
}
