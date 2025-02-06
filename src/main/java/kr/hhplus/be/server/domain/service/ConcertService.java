package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.models.Concert;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ConcertService(ConcertRepository concertRepository, RedisTemplate<String, Object> redisTemplate) {
        this.concertRepository = concertRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 공연 ID를 기반으로 공연 정보를 조회 (Redis 캐싱 적용)
     */
    public Concert getConcertByScheduleId(Long concertScheduleId) {
        String cacheKey = "concert:" + concertScheduleId;

        // 1️⃣ Redis에서 먼저 조회
        Concert cachedConcert = (Concert) redisTemplate.opsForValue().get(cacheKey);
        if (cachedConcert != null) {
            return cachedConcert;
        }

        // 2️⃣ DB에서 조회
        Long concertId = concertRepository.findConcertByScheduleId(concertScheduleId);
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid concert ID: " + concertId));

        // 3️⃣ Redis에 캐싱 (30분 유지)
        redisTemplate.opsForValue().set(cacheKey, concert, 30, TimeUnit.MINUTES);

        return concert;
    }
}