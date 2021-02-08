package com.example.redis.Repository.inner;


import com.example.redis.Repository.RedisEntity;
import org.springframework.objenesis.instantiator.ObjectInstantiator;

/**
 * 내부적으로 FactoryClass 를 이용하여 object 를 생성한다.
 * 라풀랙션 시 10% 의 퍼포먼스 향상을 준다고 한다.
 * 이런 FactoryClass 를 사용하기 위하여 Entity 를 만들때 몇가지 제약사항이있다.
 *  1. private class 가 아니여야 한다.
 *  2. non-static inner class 가 없어야 한다.
 *  3. CGLib proxy class 가 아니어야 한다.
 *  4. 생성자는 private 하지 않는 Spring data 를 사용해야 한다.
 */
public class RedisEntityInstantiator implements ObjectInstantiator {

    @Override
    public Object newInstance() {
        return new RedisEntity(null,null);
    }
    public Object newInstance(Object... args) {
        return new RedisEntity((String)args[0],(String)args[1]);
    }
}
