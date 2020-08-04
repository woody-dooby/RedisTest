package com.example.redisStream;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@SpringBootTest
@Import(com.example.config.RedisStreamConfig.class)
public class RedisStreamTest {

    @Autowired
    RedisConnectionFactory factory;

    @Autowired
    @Qualifier(value = "CustomTemplate")
    RedisTemplate<String, String> template;

    RedisConnection instance;

    private RedisConnection getInstance(){
        if(instance == null){
            instance = factory.getConnection();
        }
        return instance;
    }

    @Test
    @DisplayName("REDIS TEST")
    void simpleRedisStreamTest(){
        RedisConnection connection = getInstance();
        /* Appending */
        //connection 이용 시 MapRecord(ByteRecord 는 MapRecord 를 상속) 필요.
        //low-level
        ByteRecord lowRecord = StreamRecords.rawBytes(Collections.singletonMap("name".getBytes(),"CodingCheol".getBytes())).withStreamKey("TEST-KEY".getBytes());
        ByteRecord lowRecord2 = StreamRecords.rawBytes(Collections.singletonMap("age".getBytes(),"28".getBytes())).withStreamKey("TEST-KEY".getBytes());
        connection.xAdd(lowRecord);
        connection.xAdd(lowRecord2);
        //RedisTemplate 이용 시
        //high-level
        StringRecord highRecord = StreamRecords.string(Collections.singletonMap("name","HongGilDong")).withStreamKey("TEST-KEY");
        StringRecord highRecord2 = StreamRecords.string(Collections.singletonMap("age","20")).withStreamKey("TEST-KEY");
        template.opsForStream().add(highRecord);
        template.opsForStream().add(highRecord2);

        /* Consuming */
        //conection 이용 시 ByteRecord 는 여전하다..
        List<ByteRecord> message =  connection.xRead(
                StreamReadOptions.empty().count(2),                             // Stream Read 의 설정 옵션 정보이다. 2개까지 읽겠다는 뜻이다.
                StreamOffset.create("TEST-KEY".getBytes(), ReadOffset.latest()) // `TEST-KEY` 라는 키의 StreamOffSet 의 최신부터 메시지 가지고 옴. //StreamOffSet.latest("TEST-KEY".getBytes()) 와 같은 뜻.
        );
        if (message != null) {
            message.forEach(t->log.info("KEY : {}, Message : {}",t.getStream(),t.getValue()));
        }
        //Stream 이용 시
        List<MapRecord<String, Object, Object>> message2 =  template.opsForStream().read(
                StreamReadOptions.empty().count(2),
                StreamOffset.create("TEST-KEY", ReadOffset.lastConsumed())
        );
        if(message2 != null){
            message2.forEach(t->log.info("KEY : {}, Message : {}",t.getStream(),t.getValue()));
        }
    }

}
