package kr.hhplus.be.server.domain.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisTestService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void testRedis() {
        redisTemplate.opsForValue().set("testKey", "Hello, Redis!");
        String value = redisTemplate.opsForValue().get("testKey");
        System.out.println("Redis에서 가져온 값: " + value);
    }
}
