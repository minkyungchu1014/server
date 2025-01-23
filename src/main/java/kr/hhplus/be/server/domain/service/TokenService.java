package kr.hhplus.be.server.domain.service;

import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.api.domain.utils.TokenUtils;
import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class TokenService {

    @PersistenceContext
    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository, Clock clock ) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public String generateToken(Long userId) {
        String token;
        do {
            token = TokenUtils.generateTokenString(userId);
        } while (tokenRepository.existsByToken(token));

        Token tokenEntity = new Token();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusYears(1));

        tokenRepository.save(tokenEntity);
        return token;
    }

    /**
     * 스케줄러 기반: 만료된 토큰 삭제
     */
    public void deleteByExpiresAtBefore() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

//
//    /**
//     * 토큰 상태 변경 및 만료 시간 설정
//     */
//    public void updateTokenStatus(String token, String status, LocalDateTime expiresAt) {
//        tokenRepository.updateStatusAndExpiresAt(token, status, expiresAt);
//    }
//
    // 공통: 특정 토큰 존재 여부 확인
    @Transactional
    public boolean tokenExists(String token) {
        return tokenRepository.existsByToken(token);
    }



    public boolean isTokenExpired(String token) {
        return tokenRepository.isTokenExpired(token);
    }

//    /**
//     * 비즈니스 로직 기반: reserved 상태 5분 이상 경과
//     */
//    public boolean isTokenExpiredWithin5Minutes(String token) {
//        return tokenRepository.isTokenExpiredWithin5Minutes(token);
//    }
}
