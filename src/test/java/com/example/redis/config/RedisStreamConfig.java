package com.example.redis.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@TestConfiguration
@Import(RedisConfig.class)
public class RedisStreamConfig {

    @Bean(name = "CustomTemplate")
    public RedisTemplate setTemplate(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }

    @Bean(name = "CustomContainer")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> setMessageListenerContainer(RedisConnectionFactory factory){

        //StreamMessageListenerContainer 설정 정보
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(1000))        //요청 시간을 1초로 지정.
//                        .objectMapper(new ObjectHashMapper         //RedisConversion 으로 기본 등록이 되있는 상태이나 추가적으로 등록도 가능.
                        .build();

        return StreamMessageListenerContainer.create(factory, options);
    }

}

