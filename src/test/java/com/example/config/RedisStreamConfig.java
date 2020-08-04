package com.example.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@TestConfiguration
@Import(RedisConfig.class)
public class RedisStreamConfig {

    @Bean(name = "CustomTemplate")
    public RedisTemplate setTemplate(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }

    @Bean
    public StreamMessageListenerContainer setMessageListenerContainer(RedisConnectionFactory factory){

        //StreamMessageListenerContainer 설정 정보
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(100))        //요청 시간을 0.01초로 지정.
//                        .objectMapper()                           //TODO spring conversionService 만 등록해도 될꺼같은데.. 해보자.
                        .build();

        return StreamMessageListenerContainer.create(factory,options);
    }

}

