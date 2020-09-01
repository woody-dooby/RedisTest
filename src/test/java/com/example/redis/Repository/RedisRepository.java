package com.example.redis.Repository;

import org.springframework.data.repository.CrudRepository;

public interface RedisRepository extends CrudRepository<RedisEntity,String> {

}
