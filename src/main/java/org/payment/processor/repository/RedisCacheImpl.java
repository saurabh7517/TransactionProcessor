package org.payment.processor.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisCacheImpl implements CacheDao {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void pushOnStack(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public Object popFromStack(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public List<Object> getAllEntriesFromStack(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
