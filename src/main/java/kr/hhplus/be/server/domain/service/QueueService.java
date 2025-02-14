package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.repository.RedisRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class QueueService {
    private final RedisRepository redisRepository;
    private static final String QUEUE_USERS_KEY = "queue:users";

    public QueueService(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }
    /**
     * 사용자 대기열 추가 (WAITING)
     */
    @Transactional
    public void addToQueue(String tokenId, Long userId) {
        long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000);
        String value = userId + ":WAITING:" + tokenId;
        removeFromQueue(userId);
        redisRepository.addToQueue(QUEUE_USERS_KEY, value, expiresAt);
    }


    /**
     * 사용자 활성화 (WAITING → ACTIVE)
     */
    @Transactional
    public void activateTokens(int maxActiveTokens) {
        Set<String> users = redisRepository.getQueue(QUEUE_USERS_KEY);
        int activatedCount = 0;

        for (String userData : users) {
            if (activatedCount >= maxActiveTokens) break;
            String[] parts = userData.split(":");
            if (parts[1].equals("WAITING")) {
                long expiresAt = System.currentTimeMillis() + (30 * 60 * 1000);
                String newValue = parts[0] + ":ACTIVE:" + parts[2];
                redisRepository.removeFromQueue(QUEUE_USERS_KEY, userData);
                redisRepository.addToQueue(QUEUE_USERS_KEY, newValue, expiresAt);
                activatedCount++;
            }
        }
    }

    @Transactional
    public String getQueueStatusByToken(String tokenId) {
        Set<String> users = redisRepository.getQueue(QUEUE_USERS_KEY);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[2].equals(tokenId)) {
                return parts[1];
            }
        }
        return "NOT_FOUND";
    }

    public boolean isQueueActive(String tokenId) {
        Set<String> users = redisRepository.getQueue(QUEUE_USERS_KEY);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[2].equals(tokenId) && parts[1].equals("ACTIVE")) {
                return true;
            }
        }
        return false;
    }

    public void updateQueueStatus(String tokenId, LocalDateTime expiresAt) {
        Set<String> users = redisRepository.getQueue(QUEUE_USERS_KEY);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[2].equals(tokenId)) {
                String newValue = parts[0] + ":ACTIVE:" + parts[2];
                long epochMillis = expiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                redisRepository.removeFromQueue(QUEUE_USERS_KEY, userData);
                redisRepository.addToQueue(QUEUE_USERS_KEY, newValue, epochMillis);
                return;
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void deleteByExpiresAtBefore() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredUsers = redisRepository.getQueue(QUEUE_USERS_KEY);

        for (String userData : expiredUsers) {
            redisRepository.removeFromQueue(QUEUE_USERS_KEY, userData);
            String[] parts = userData.split(":");
            updateUserStatusInDB(Long.parseLong(parts[0]), "EXPIRED");
        }
    }

    @Scheduled(fixedRate = 60000)
    public void removeExpiredActiveTokens() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredUsers = redisRepository.getQueue(QUEUE_USERS_KEY);

        for (String userData : expiredUsers) {
            redisRepository.removeFromQueue(QUEUE_USERS_KEY, userData);
            String[] parts = userData.split(":");
            updateUserStatusInDB(Long.parseLong(parts[0]), "EXPIRED");
        }
    }

    @Transactional
    public void removeFromQueue(Long userId) {
        Set<String> users = redisRepository.getQueue(QUEUE_USERS_KEY);

        for (String userData : users) {
            String[] parts = userData.split(":");
            if (parts[0].equals(userId.toString())) {
                redisRepository.removeFromQueue(QUEUE_USERS_KEY, userData);
                return;
            }
        }
    }

    private void updateUserStatusInDB(Long userId, String status) {
        System.out.println("DB 상태 업데이트: userId=" + userId + ", status=" + status);
    }
}
