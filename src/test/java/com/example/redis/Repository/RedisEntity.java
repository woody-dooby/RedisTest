package com.example.redis.Repository;

import lombok.Data;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.beans.ConstructorProperties;

/**
 * Spring Data Redis 에서는 Entity 를 생성할때 몇가지 추천 방향이 있다.
 * 1. 불변의 객체를 고수하라.
 * 2. 모든 파라미터를 받는 생성자를 제공하라.
 * 3. @PersistenceConstructor 를 피하기 위하여 Factory Method 를 사용하라.
 */
@RedisHash("RedisEntity")       //keySpace
@Data
public class RedisEntity {
    @Id                         //Id - key 값
    private final String id;
    private String name;
    //@AccessType : MethodHandles 의 사용 없이 직접 접근이 가능하게끔함.
    private @AccessType(AccessType.Type.PROPERTY) String address;

    @PersistenceConstructor
    @ConstructorProperties({"id","name","address"})
    public RedisEntity(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }
    public RedisEntity(String name, String address) {
        this.id = null;
        new RedisEntity(null,name,address);
    }
    public RedisEntity withId(String id){
        return new RedisEntity(id, this.name ,this.address);
    }
}
