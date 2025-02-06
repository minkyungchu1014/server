package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.api.domain.utils.TokenUtils;
import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TokenService(TokenRepository tokenRepository, StringRedisTemplate redisTemplate) {
        this.tokenRepository = tokenRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * UUID 기반으로 토큰을 생성하고 저장
     * @param userId 사용자 ID
     * @return 생성된 토큰 값
     */
    @Transactional
    public String generateToken(Long userId) {
        if (tokenRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 발급된 토큰이 존재합니다.");
        }

        // UUID 기반 토큰 생성
        String token = TokenUtils.generateTokenString(userId);

        Token tokenEntity = new Token();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusYears(1)); // 1년 후 만료

        tokenRepository.save(tokenEntity);

        // Redis에 30분 동안 저장 (기본적으로 WAIT 상태)
        redisTemplate.opsForValue().set(token, "WAIT", 30, TimeUnit.MINUTES);

        return token;
    }

    /**
     * Redis에서 토큰이 존재하는지 확인
     * @param token 확인할 토큰 값
     * @return 토큰이 존재하면 true, 없으면 false
     */
    public boolean isTokenInRedis(String token) {
        return "WAIT".equals(redisTemplate.opsForValue().get(token));
    }

    /**
     * 토큰을 활성화 (WAIT → ACTIVE)
     * @param token 활성화할 토큰 값
     */
    public void activateToken(String token) {
        redisTemplate.opsForValue().set(token, "ACTIVE", 30, TimeUnit.MINUTES);
    }

    /**
     * 만료된 토큰 삭제
     */
    @Transactional
    public void deleteByExpiresAtBefore() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    /**
     * 토큰이 존재하는지 확인
     * @param token 확인할 토큰 값
     * @return 존재 여부
     */
    @Transactional
    public boolean tokenExists(String token) {
        return tokenRepository.existsByToken(token);
    }

    /**
     * 토큰이 만료되었는지 확인
     * @param token 확인할 토큰 값
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        return tokenRepository.isTokenExpired(token);
    }
}
