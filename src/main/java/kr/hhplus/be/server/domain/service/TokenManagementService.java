package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.api.domain.utils.TokenUtils;
import kr.hhplus.be.server.domain.models.Token;
import kr.hhplus.be.server.domain.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class TokenManagementService {
    private final TokenRepository tokenRepository;
    private final Clock clock;

    @Autowired
    public TokenManagementService(TokenRepository tokenRepository, Clock clock) {
        this.tokenRepository = tokenRepository;
        this.clock = clock;
    }

    /**
     * 새 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 토큰 값
     */
    @Transactional
    public String generateToken(Long userId) {
        String token;
        do {
            token = TokenUtils.generateTokenString(userId);
        } while (tokenRepository.existsByToken(token));

        Token tokenEntity = new Token();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(token);
        tokenEntity.setExpiresAt(LocalDateTime.now(clock).plusMinutes(30));

        tokenRepository.save(tokenEntity);
        return token;
    }

}
