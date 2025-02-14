package kr.hhplus.be.server.domain.infrastructure;

import kr.hhplus.be.server.domain.repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRepositoryImpl implements RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisRepositoryImpl(RedisTemplate<String, String> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void addToQueue(String key, String value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public void removeFromQueue(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    @Override
    public Set<String> getQueue(String key) {
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    @Override
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void setValue(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }
}