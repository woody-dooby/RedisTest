package com.example.redis;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class RedisApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(RedisApplication.class)
                .web(WebApplicationType.SERVLET)
                .build()
                .run(args);
    }

}
