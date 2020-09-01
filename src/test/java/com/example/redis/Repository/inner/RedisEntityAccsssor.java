package com.example.redis.Repository.inner;

import com.example.redis.Repository.RedisEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

import java.lang.invoke.MethodHandle;

/**
 * Entity 의 property 값들을 다루기 위하여 내부적으로 Accessor class 를 만들어 다룬다.
 * 리플렉션 시 25%의 퍼포먼스 향상을 준다고 한다.
 * Accessor 를 생성 시 몇가지 규칙이 존재하는데 아래와 같다.
 *  1. final 같은 불변의 변수들이 with... 메서드가 존재할 경우 해당 메서드를 사용하여 값을 적용한다.
 *  2. setter 가 정의되어 있을 경우 해당 메서드를 사용한다.
 *  3. 변할수있는 변수가 선언되어 있을 경우 직접적으로 세팅한다.
 *  4. 불변의 변수가 선언되었을 경우 persistence operation( Instantiator ) 을 사용하여 생성한다.
 *  5. 기본적으로 직접적으로 변수에 할당한다.
 *
 * Accessor 를 생성 시 몇가지 제약이 존재하는데 아래와 같다.
 *  1. public 으로 선언 되어 있어야 한다.
 *  2.
 */
public class RedisEntityAccsssor implements PersistentPropertyAccessor {

    private static MethodHandle nameHandler;

    private RedisEntity redisEntity;

    @Override
    public void setProperty(PersistentProperty property, Object value) {
        String name = property.getName();

        if("name".equals(name)){
            try {
                nameHandler.invoke(redisEntity,(String)value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }else if("id".equals(name)){
            this.redisEntity = redisEntity.withId((String)value);
        }else if("address".equals(name)){
            this.redisEntity.setAddress((String)value);
        }
    }

    @Override
    public Object getProperty(PersistentProperty property) {
        return null;
    }

    @Override
    public Object getBean() {
        return null;
    }
}
