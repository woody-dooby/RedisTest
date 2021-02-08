package com.example.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.hash.ObjectHashMapper;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@Import(com.example.redis.config.RedisEmbeddedConfig.class)
public class SDRTests {

    @Autowired
    private RedisConnectionFactory factory;

    @Test
    void lettuceTest(){
        LettuceConnection connection = (LettuceConnection) factory.getConnection();

        RedisListCommands commands = connection.listCommands();
        commands.lPush("1234".getBytes(),"가".getBytes());
        commands.lPush("1234".getBytes(),"나".getBytes());
        commands.lPush("1234".getBytes(),"STOP".getBytes());
        commands.lPush("1234".getBytes(),"다".getBytes());
        commands.lPush("1234".getBytes(),"라".getBytes());

        assertEquals(new String(Objects.requireNonNull(connection.listCommands().lPop("1234".getBytes()))),"라");
        assertEquals(new String(Objects.requireNonNull(connection.listCommands().rPop("1234".getBytes()))),"가");
        assertEquals(new String(Objects.requireNonNull(connection.listCommands().lPop("1234".getBytes()))),"다");
        assertEquals(new String(Objects.requireNonNull(connection.listCommands().rPop("1234".getBytes()))),"나");
    }

    @Test
    void stringTemplateTest(){
        StringRedisTemplate template = new StringRedisTemplate(factory);
        ValueOperations<String, String> list=  template.opsForValue();

        list.set("test","가");
        list.set("test2","나");
        list.set("test3","다");
        list.set("test4","라");

        assertEquals( template.opsForValue().get("test"),"가");
        assertEquals( template.opsForValue().get("test2"),"나");
        assertEquals( template.opsForValue().get("test3"),"다");
        assertEquals( template.opsForValue().get("test4"),"라");
    }

    @Test
    void stringTemplateCallBackTest(){
        StringRedisTemplate template = new StringRedisTemplate(factory);

        //선 작업 할수있음.
        String ss = template.execute((RedisCallback<String>) (connection)->{
            Long size = connection.dbSize(); //Spring default connection : 26
            ((StringRedisConnection)connection).set("test","가");
            return String.valueOf(size);
        });

        assertEquals("26",ss);

        log.info("connection data set : {}",template.opsForValue().get("test"));

        assertEquals("가",template.opsForValue().get("test"));
    }

    @Data
    static class AClass{
        String type;
        BClass bClass;

    }
    @Data
    @AllArgsConstructor
    static class BClass{
        String code;
        String name;
    }
    @Test
    void hashMapperTest(){
        RedisTemplate template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        /*TODO Serializer 구현*/
        /*Obj Mapper*/
        HashMapper<Object,byte[],byte[]> objMapper = new ObjectHashMapper();
        BClass bClass = new BClass("testCode","testName");

        Map<byte[], byte[]> map = objMapper.toHash(bClass);
        template.opsForHash().putAll("testObj",map);

        BClass mappedBClass = (BClass) objMapper.fromHash(template.opsForHash().entries("testObj"));
        assertEquals(mappedBClass.getCode(), "testCode");
        assertEquals(mappedBClass.getName(), "testName");

        /*Jackson Mapper*/
        /*TODO Serializer 구현*/
        HashMapper jacksonMapper = new Jackson2HashMapper(true);
        AClass aClass = new AClass();
        aClass.setType("redis");
        aClass.setBClass(bClass);

        Map map2 = jacksonMapper.toHash(aClass);
        template.opsForHash().putAll("testObj2",map2);

        AClass mappedAClass = (AClass) objMapper.fromHash(template.opsForHash().entries("testObj2"));
        log.info(mappedAClass.getType());
    }
}
