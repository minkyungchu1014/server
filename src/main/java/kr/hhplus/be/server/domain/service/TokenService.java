package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.api.domain.utils.TokenUtils;
import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.repository.RedisRepository;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final RedisRepository redisRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository, RedisRepository redisRepository) {
        this.tokenRepository = tokenRepository;
        this.redisRepository = redisRepository;
    }

    @Transactional
    public String generateToken(Long userId) {
        if (tokenRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 발급된 토큰이 존재합니다.");
        }

        String token = TokenUtils.generateTokenString(userId);

        Token tokenEntity = new Token();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusYears(1));

        tokenRepository.save(tokenEntity);

        redisRepository.setValue(token, "WAIT", 30, TimeUnit.MINUTES);

        return token;
    }

    public boolean isTokenInRedis(String token) {
        return "WAIT".equals(redisRepository.getValue(token));
    }

    public void activateToken(String token) {
        redisRepository.setValue(token, "ACTIVE", 30, TimeUnit.MINUTES);
    }

    @Transactional
    public void deleteByExpiresAtBefore() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Transactional
    public boolean tokenExists(String token) {
        return tokenRepository.existsByToken(token);
    }

    public boolean isTokenExpired(String token) {
        return tokenRepository.isTokenExpired(token);
    }
}
