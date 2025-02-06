package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class QueueService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_USERS_KEY = "queue:users";

    public QueueService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 사용자 대기열 추가 (WAITING)
     */
    @Transactional
    public void addToQueue(String tokenId, Long userId) {
        long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000); // 30분 후 만료
        String value = userId + ":WAITING:" + tokenId;

        // 기존 토큰 삭제 (1인 1토큰 정책 유지)
        removeFromQueue(userId);

        redisTemplate.opsForZSet().add(QUEUE_USERS_KEY, value, expiresAt);
    }

    /**
     * 사용자 활성화 (WAITING → ACTIVE)
     */
    @Transactional
    public void activateTokens(int maxActiveTokens) {
        Set<String> users = redisTemplate.opsForZSet().range(QUEUE_USERS_KEY, 0, -1);
        int activatedCount = 0;

        for (String userData : users) {
            if (activatedCount >= maxActiveTokens) break;

            String[] parts = userData.split(":");
            if (parts[1].equals("WAITING")) {
                long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000);
                String newValue = parts[0] + ":ACTIVE:" + parts[2];

                redisTemplate.opsForZSet().remove(QUEUE_USERS_KEY, userData);
                redisTemplate.opsForZSet().add(QUEUE_USERS_KEY, newValue, expiresAt);
                activatedCount++;
            }
        }
    }

    @Transactional
    public String getQueueStatusByToken(String tokenId) {
        Set<String> users = redisTemplate.opsForZSet().range(QUEUE_USERS_KEY, 0, -1);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[2].equals(tokenId)) {
                return parts[1]; // "WAITING" 또는 "ACTIVE"
            }
        }
        return "NOT_FOUND";
    }

    public boolean isQueueActive(String tokenId) {
        Set<String> users = redisTemplate.opsForZSet().range(QUEUE_USERS_KEY, 0, -1);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[2].equals(tokenId) && parts[1].equals("ACTIVE")) {
                return true;
            }
        }
        return false;
    }


    public void updateQueueStatus(String tokenId, LocalDateTime expiresAt) {
        Set<String> users = redisTemplate.opsForZSet().range(QUEUE_USERS_KEY, 0, -1);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[2].equals(tokenId)) {
                String newValue = parts[0] + ":ACTIVE:" + parts[2];

                // ✅ `LocalDateTime` → `EpochMilli` 변환 (수정된 코드)
                long epochMillis = expiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

                redisTemplate.opsForZSet().remove(QUEUE_USERS_KEY, userData);
                redisTemplate.opsForZSet().add(QUEUE_USERS_KEY, newValue, epochMillis);
                return;
            }
        }
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void deleteByExpiresAtBefore() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredUsers = redisTemplate.opsForZSet().rangeByScore(QUEUE_USERS_KEY, 0, currentTime);

        for (String userData : expiredUsers) {
            redisTemplate.opsForZSet().remove(QUEUE_USERS_KEY, userData);

            // DB 상태 업데이트 트리거
            String[] parts = userData.split(":");
            updateUserStatusInDB(Long.parseLong(parts[0]), "EXPIRED");
        }
    }

    /**
     * 만료된 사용자 삭제
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void removeExpiredActiveTokens() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredUsers = redisTemplate.opsForZSet().rangeByScore(QUEUE_USERS_KEY, 0, currentTime);

        for (String userData : expiredUsers) {
            redisTemplate.opsForZSet().remove(QUEUE_USERS_KEY, userData);

            // DB 상태 업데이트 트리거 실행
            String[] parts = userData.split(":");
            updateUserStatusInDB(Long.parseLong(parts[0]), "EXPIRED");
        }
    }

    @Transactional
    public void removeFromQueue(Long userId) {
        Set<String> users = redisTemplate.opsForZSet().range(QUEUE_USERS_KEY, 0, -1);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[0].equals(userId.toString())) {
                redisTemplate.opsForZSet().remove(QUEUE_USERS_KEY, userData);
                return;
            }
        }
    }

    private void updateUserStatusInDB(Long userId, String status) {
        // DB에서 해당 사용자의 상태를 "EXPIRED"로 변경
        System.out.println("DB 상태 업데이트: userId=" + userId + ", status=" + status);
    }
}
