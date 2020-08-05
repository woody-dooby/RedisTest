package com.example.redis.redisPipelined;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

@Slf4j
@SpringBootTest
@Import(com.example.redis.config.RedisConfig.class)
public class RedisPipelined {

    RedisTemplate<String, String> template;
    @BeforeEach
    void init(@Autowired RedisConnectionFactory factory){
        this.template = new StringRedisTemplate(factory);
    }

    private final Integer size = 10000;

    @Test
    void templatePipelinedTest(){
        //기본적으로 결과값은 String 값이나 추가적인 Serializer 를 추가하면 해당 Object로 사용할 수 있다.
        List<Object> results = template.executePipelined((RedisConnection connection)->{
            connection.multi();
            for(int i = 0; i< size ; i++){
                ((StringRedisConnection)connection).rPush("TEST"+i,"gg");
            }
            //만약 해당 파이프 라인의 결과를 생각하지 않는다면 BOOLEAN 값이나, NUll 값을 사용해도 무방하다.
            return connection.exec();
        });
        Assertions.assertEquals(1,results.size());
        for(int i = 0; i< size ; i++){
            log.info("RESULT : {}",template.getConnectionFactory().getConnection().rPop(("TEST"+i).getBytes()));
        }
    }
    @Test
    void pipelinedTest(){
        RedisConnection connection = template.getConnectionFactory().getConnection();

        connection.openPipeline();
        for(int i = 0; i< size ; i++){
            ((StringRedisConnection)connection).rPush("TEST"+i,"gg");
        }
        connection.closePipeline();

        for(int i = 0; i< size ; i++){
            log.info("RESULT : {}",template.getConnectionFactory().getConnection().rPop(("TEST"+i).getBytes()));
        }
    }
    @Test
    void benchmark(){
        ValueOperations<String, String> operations =  template.opsForValue();
        long start = System.currentTimeMillis();

        for(int i = 0; i< size ; i++){
            operations.set("TEST"+i,"N-VALUE");
        }
        long normal = System.currentTimeMillis() - start;
        log.info("NORMAL TIME(Ms) : {}",normal );

        start = System.currentTimeMillis();

        template.executePipelined((RedisConnection connection)->{
            for(int i = 0; i< size ; i++){
                ((StringRedisConnection)connection).rPush("PIPLETEST"+i,"P-VALUE");
            }
            return null;
        });
        long pipelined = System.currentTimeMillis() - start;
        log.info("PIPELINE TIME(Ms) : {}",pipelined );
        //월등한 차이가 보인다..
        Assertions.assertTrue(pipelined<normal);
    }

}
