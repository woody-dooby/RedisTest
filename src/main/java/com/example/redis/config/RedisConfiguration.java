package com.example.redis.config;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Spring Boot 의 Auto Config Spring.factories 안에 RedisAutoConfiguration.java 가 메타 데이터로 잡혀있어서
 * 데이터의 중복이 발생하게 될 것이다. 시작 시 application 설정 파일을 이용하여 설정이 가능하지만, 우리는 연습이기에 RedisAutoConfiguration.java 에 대한
 * 설정을 RedisApplication.java 에서 exclude 하고 여기서 재정의하여 사용하겠다.
 * --> AutoConfig 방식을 잘 따라기만 하면 우리도 해당 설정을 쉽게 접근 할 수 있다.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisClient.class)
//우리는 배운 사람들이기에 설정파일이 존재하는 여부를 체크해보자
@ConditionalOnProperty(prefix = "spring.redis")
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfiguration {
    //기본적으로 `clusterConfiguration` 을 application 설정으로 처리하도록 함.  --> 나중을 위해서라도 사용을 추구,
    private RedisProperties properties;
    private RedisClusterConfiguration clusterConfiguration;

    protected RedisConfiguration(RedisProperties properties, ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        this.properties = properties;
        this.clusterConfiguration = clusterConfigurationProvider.getIfAvailable();
    }

    @Bean
    public LettuceConnectionFactory getRedisConnectionFactory(){
        //Lettuce 클라이언트 설정
        //단일 connection 설정 시는 이게 맞으나... 나는 여러개의 connection 을 다루는 connection pool 을 다룰껏이다.

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .commandTimeout(Duration.ofMinutes(1))
                .shutdownTimeout(Duration.ZERO)
                .build();
        return new LettuceConnectionFactory(clusterConfiguration, clientConfiguration);
    }

    @Bean
    public RedisTemplate getRedisTemplate(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }
}
