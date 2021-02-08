package com.example.redis.redisStream;

import com.example.redis.config.RedisStreamConfig;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
@Import(RedisStreamConfig.class)
public class RedisStreamTest {

    @Autowired
    RedisConnectionFactory factory;

    @Autowired
    @Qualifier(value = "CustomTemplate")
    RedisTemplate<String, String> template;

    @Autowired
    @Qualifier(value = "CustomContainer")
    StreamMessageListenerContainer container;

    RedisConnection instance;
    private RedisConnection getInstance(){
        if(instance == null){
            instance = factory.getConnection();
        }
        return instance;
    }

    @Test
    void simpleRedisStreamTest(){
        RedisConnection connection = getInstance();
        /* Appending */
        //connection 이용 시 MapRecord(ByteRecord 는 MapRecord 를 상속) 필요.
        //low-level
        ByteRecord lowRecord = StreamRecords.rawBytes(Collections.singletonMap("name".getBytes(),"CodingCheol".getBytes())).withStreamKey("TEST-KEY".getBytes());
        ByteRecord lowRecord2 = StreamRecords.rawBytes(Collections.singletonMap("age".getBytes(),"28".getBytes())).withStreamKey("TEST-KEY".getBytes());
        connection.xAdd(lowRecord);
        RecordId id = connection.xAdd(lowRecord2);
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
                StreamOffset.create("TEST-KEY".getBytes(), ReadOffset.from("0-0")) // `TEST-KEY` 라는 키의 StreamOffSet 의 처음부터 메시지 가지고 옴.
        );
        if (message != null) {
            // 눈으로만 보아도 저 레밸 별루.
            message.forEach(t->log.info("KEY : {}, Message : {}",new String(t.getStream()),
                    t.getValue().entrySet().stream().collect(Collectors.toMap(a->new String(a.getKey()),a->new String(a.getValue())))));
        }
        //Stream 이용 시
        List<MapRecord<String, Object, Object>> message2 =  template.opsForStream().read(
                StreamReadOptions.empty().count(2),
                StreamOffset.create("TEST-KEY", ReadOffset.from(id))
        );
        if(message2 != null){
            message2.forEach(t->log.info("KEY : {}, Message : {}",t.getStream(),t.getValue()));
        }
    }

    @Test
    void consumerGroupStream(){
        //Synchronized -> Asysnchronized
        Executor executor = Executors.newFixedThreadPool(5,(runnable)->{Thread t = new Thread(runnable); t.setDaemon(true); return t;});
        CompletableFuture< List<MapRecord<String, Object, Object>>> conumer =
                CompletableFuture.supplyAsync(()->
                        // `CLI` 로 `Consumer Group`을 key 매칭하여 생성한 후에 사용됨을 주의하자!
                        // 형식을 잘 봐야한다.
                        // Consumer 는 여기서 정해준 것이 전부이기에 여기서 추가하게 되면 컨슈머가 추가됨을 알아야하며, 주입되는 인자들에 대하여 익숙해져야 할꺼같다.
                        // 1. Consumer.from(group, name)
                        // 2. StreamReadOption
                        // 3. StreamOffset
                        // kafka 와 비교 해보아도 정말 offSet 관리가 비교적 간편하다.
                        // 그렇지만 appending(=producer)하는 입장에서는 consumer(=topic) 자체를 지정하는 것이 아닌 redis에 보내는 데이터 이기에 해당 데이터를 처리하는 것에 대한 각별한 신경이 필요해 보인다.
                        template.opsForStream().read(
                            Consumer.from("GC-1","consumer1"),
                            StreamReadOptions.empty().count(2),
                            StreamOffset.create("TEST-KEY",ReadOffset.lastConsumed()))
                ,executor);

        StringRecord record1 = StreamRecords.string(Collections.singletonMap("name","CodingCheol")).withStreamKey("TEST-KEY");
        StringRecord record2 = StreamRecords.string(Collections.singletonMap("age","20")).withStreamKey("TEST-KEY");

        template.opsForStream().add(record1);
        template.opsForStream().add(record2);

        conumer.join().forEach(t->log.info("KEY : {}, Message : {}",t.getStream(),t.getValue()));
    }

    @Test
    void messageListenerContainerTest() throws InterruptedException {
        AtomicInteger integer = new AtomicInteger(0);
        //Asynchronized
        Subscription subscription = container.receive(
                Consumer.from("GC-1","Container1"),
                StreamOffset.create("TEST-KEY",ReadOffset.lastConsumed()),
//                StreamOffset.fromStart("TEST-KEY"),
                (message)->{
                    log.info("KEY : {}, Message : {}",message.getStream(),message.getValue());
                    integer.getAndIncrement();
                });
        //이거 해줘야함..
        container.start();


        Executor executor = Executors.newFixedThreadPool(5,(runnable)->{Thread t = new Thread(runnable); t.setDaemon(true); return t;});
        IntStream.range(0,100).forEach(t->{
            CompletableFuture.runAsync(()->{
                StringRecord record1 = StreamRecords.string(Collections.singletonMap("name","CodingCheol")).withStreamKey("TEST-KEY");
                StringRecord record2 = StreamRecords.string(Collections.singletonMap("age","20")).withStreamKey("TEST-KEY");
                template.opsForStream().add(record1);
                template.opsForStream().add(record2);
            },executor);
        });

        Thread.sleep(5000);

        container.remove(subscription);
        container.stop(()->{
            log.info("count : {}",integer.get());
        });
    }
    @Test
    void consumerGroupStreamCustomObjectTest(){
        //ObjectRecord
        ObjectRecord record = StreamRecords.newRecord().in("TEST-KEY")
                .ofObject(new RedisStreamCustomObject("codingcheol",28,"programmer"));
        template.opsForStream().add(record);

        //ObjectRecord
        ObjectRecord record2 = StreamRecords.newRecord().in("TEST-KEY")
                .ofObject(new RedisStreamCustomObject("Hong-Gil-Dong",28,"programmer"));
        template.opsForStream().add(record2);

        List<ObjectRecord<String, RedisStreamCustomObject>> object = template.opsForStream().read(
                RedisStreamCustomObject.class,
                Consumer.from("GC-1","OBJECT"),
                StreamOffset.create("TEST-KEY",ReadOffset.from(">")));

        object.forEach(t->log.info("KEY : {}, Message : [name : {}, age : {}, job : {} ]",t.getStream(),t.getValue().getName(),t.getValue().getAge(),t.getValue().getJob()));
    }

    @Test
    void messageListenerContainerCustomObjectTest() throws InterruptedException {
        AtomicInteger integer = new AtomicInteger(0);
        //Asynchronized
        Subscription subscription = container.receive(
                Consumer.from("GC-1","Container2"),
                StreamOffset.create("TEST-KEY",ReadOffset.lastConsumed()),
                (message)->{
                    log.info("KEY : {}, Message : {}",message.getStream(),message.getValue());
                    integer.getAndIncrement();
                });
        container.start();

        //ObjectRecord
        ObjectRecord record = StreamRecords.newRecord().in("TEST-KEY")
                .ofObject(new RedisStreamCustomObject("codingcheol",28,"programmer"));
        template.opsForStream().add(record);

        Thread.sleep(10000);
        container.stop(()->{
            log.info("END");
        });
    }
}
