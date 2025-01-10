package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.repository.TokenRepository;
import kr.hhplus.be.server.domain.repository.QueueRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final QueueRepository queueRepository; // 대기열 정보를 관리하는 Repository

    public TokenService(TokenRepository tokenRepository, QueueRepository queueRepository) {
        this.tokenRepository = tokenRepository;
        this.queueRepository = queueRepository;
    }

    /**
     * 사용자에게 활성화된 토큰이 있는지 확인합니다.
     * @param userId 사용자 ID
     * @return 활성화된 토큰이 존재하면 true, 없으면 false
     */
    public boolean hasActiveToken(Long userId) {
        return tokenRepository.existsByUserId(userId);
    }

    /**
     * 유저 UUID, 대기열 정보를 포함하는 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 토큰 (JSON 문자열)
     */
    public String createToken(Long userId) {
        // 토큰 정보 생성
        String token;
        token = generateTokenString(userId).toString();

        // 토큰 저장 (대기열 순번을 파악할 수 있는 user_id값이 포함되었음)
        tokenRepository.saveToken(userId, token, LocalDateTime.now().plusMinutes(30));
        return token;
    }

    /**
     * JSON 형식의 토큰 문자열 생성
     * @param userId 사용자 ID
     * @return 생성된 UUID
     */
    private static UUID generateTokenString(Long userId){
        try {
            // 사용자 ID를 문자열로 변환
            String input = String.valueOf(userId);

            // SHA-256 해시 생성
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // 해시의 첫 16바이트를 사용하여 UUID 생성
            long mostSigBits = bytesToLong(hash, 0);
            long leastSigBits = bytesToLong(hash, 8);

            return new UUID(mostSigBits, leastSigBits);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("UUID 생성에 실패했습니다.", e);
        }
    }

    /**
     * 바이트 배열을 long 값으로 변환
     * @param bytes 바이트 배열
     * @param offset 시작 위치
     * @return 변환된 long 값
     */
    private static long bytesToLong(byte[] bytes, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (bytes[offset + i] & 0xFF);
        }
        return value;
    }

    /**
     * 토큰 상태 변경 및 만료 시간 설정
     */
    public void updateTokenStatus(String token, String status, LocalDateTime expiresAt) {
        tokenRepository.updateStatusAndExpiresAt(token, status, expiresAt);
    }

    // 공통: 특정 토큰 존재 여부 확인
    public boolean tokenExists(String token) {
        return tokenRepository.existsByToken(token);
    }

    // 스케줄러 기반: 비활성화된 토큰 활성화
    public void activateTokens(int maxActiveTokens) {
        // 현재 활성화된 토큰 수를 가져오기
        int currentActiveTokens = queueRepository.countTokensByStatus("ACTIVE");

        // 활성화해야 할 토큰 수 계산
        int tokensToActivate = maxActiveTokens - currentActiveTokens;

        // 활성화해야 할 토큰 수가 없으면 종료
        if (tokensToActivate <= 0) {
            System.out.println("No tokens to activate. Current active tokens: " + currentActiveTokens);
            return;
        }

        // WAITING 상태의 토큰 가져오기
        List<String> inactiveTokens = tokenRepository.findTokensByStatusWaiting(tokensToActivate);

        // 토큰 상태를 ACTIVE로 변경
        for (String token : inactiveTokens) {
            // WAITING 상태를 ACTIVE로 변경하고 만료 시간 설정
            tokenRepository.updateStatusAndExpiresAtForWaitingTokens("ACTIVE", LocalDateTime.now().plusMinutes(10));
            System.out.println("Activated token: " + token);
        }
    }

    public void invalidateExistingTokens(Long userId) {
        tokenRepository.deleteByUserId(userId);
    }
    /**
     * 스케줄러 기반: 만료된 토큰 삭제
     */
    public void expireTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
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
