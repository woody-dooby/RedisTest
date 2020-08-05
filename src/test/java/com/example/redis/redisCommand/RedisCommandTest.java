package com.example.redis.redisCommand;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

@SpringBootTest
@Import(com.example.redis.config.RedisConfig.class)
public class RedisCommandTest{

    @Autowired
    RedisConnectionFactory factory;


    @Test
    void multiExecTest(){
        RedisConnection connection = factory.getConnection();
        // Block Start
        // normal -> transaction
        connection.multi();
        // Set Data
        for(int i = 0 ; i<10000 ; i++){
            connection.set(("Key :"+i).getBytes(), ("value:"+i).getBytes());
        }
        // Start <- 이때 끼어 들 수 가 없다.
        // exec 는 multi 를 시작한 block 영역에 대한 값만을 표시한다.
        List<Object> result = connection.exec();
        Assertions.assertEquals(10000,result.size());
    }
    @Test
    void discardTest(){
        RedisConnection connection = factory.getConnection();
        // Block Start
        // normal -> transaction
        connection.multi();
        // Set Data
        for(int i = 0 ; i<10000 ; i++){
            connection.set(("Key :"+i).getBytes(), ("value:"+i).getBytes());

            if(i == 5000){
                // transaction -> normal
                connection.discard();
                // normal -> transaction
                connection.multi();
            }
        }
        // Start <- 이때 끼어 들 수 가 없다.
        List<Object> result = connection.exec();
        Assertions.assertEquals(5000 -1,result.size());
    }

    @Test
    void watchTest(){
        RedisConnection connection = factory.getConnection();

        connection.watch(("Key :2").getBytes());

        connection.set(("Key :2").getBytes(),("22").getBytes());

        connection.multi();
        for(int i = 0 ; i<10000 ; i++){
            connection.set(("Key :"+i).getBytes(), ("value:"+i).getBytes());
        }
        //결과는 어떻게 되었을까?
        // >> {`key:1`:`22`} 만 저장되어 있을것이다.
        // pf) multi 로 transaction mode 가 되었고 아래의 exec 로 실행 되었다. 하지만 watch 명령어로 하여금 exec 수행될때 값이 변경된 값들이 반영되지 않고 취소 된다.

        List<Object> result = connection.exec();
        Assertions.assertNull(result);
    }
}
