package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.domain.models.Queue;
import kr.hhplus.be.server.api.domain.dto.QueueStatusResponse;
import kr.hhplus.be.server.domain.service.QueueService;
import kr.hhplus.be.server.domain.service.TokenService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * TokenFacade
 * - 토큰 및 대기열과 관련된 고수준 비즈니스 로직을 처리하는 클래스.
 * - Service 계층을 조합하여 클라이언트가 직접 호출하는 컨트롤러와 상호작용.
 */
@Component
public class TokenFacade {

    private final TokenService tokenService;
    private final QueueService queueService;

    public TokenFacade(TokenService tokenService, QueueService queueService) {
        this.tokenService = tokenService;
        this.queueService = queueService;
    }

    /**
     * 사용자에게 새 토큰을 생성합니다.
     * @param userId 토큰을 생성할 사용자 ID
     * @return 생성된 토큰 값
     */
    public String generateToken(Long userId) {

        // 기존 토큰 존재 여부 확인(ACTIVE/WAITING 모두)
        if (tokenService.hasActiveToken(userId)) {
            // 기존 토큰 무효화
            tokenService.invalidateExistingTokens(userId);
        }
        // 새 토큰 생성
        return tokenService.createToken(userId);
    }

    /**
     * 대기열 상태를 확인합니다.
     * @param tokenId 토큰 값
     * @return QueueStatusResponse (대기열 상태)
     * @throws IllegalArgumentException 유효하지 않은 토큰일 경우 예외 발생
     */
    public QueueStatusResponse getQueueStatus(String tokenId) {
        // 토큰 유효성 확인
        if (!tokenService.tokenExists(tokenId)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // QueueService를 통해 대기열 상태 확인
        Optional<Queue> queueOptional = queueService.findQueueEntryByTokenId(tokenId);
        QueueStatusResponse queueStatusResponse = new QueueStatusResponse();

        if (queueOptional.isPresent()) {
            Queue queue = queueOptional.get();

            // 대기열 상태 정보 설정
            queueStatusResponse.setTokenId(queue.getTokenId());
            queueStatusResponse.setStatus(queue.getStatus());
            queueStatusResponse.setExpiresAt(queue.getExpiresAt());

            // WAITING 상태인 경우 대기 순서 계산
            if ("WAITING".equals(queue.getStatus())) {
                long waitingPosition = queueService.getQueueOrderByToken(tokenId);
                queueStatusResponse.setQueuePosition(waitingPosition);
            } else {
                queueStatusResponse.setQueuePosition(0L);
            }
        } else {
            // 대기열 항목이 없는 경우 기본 상태 반환
            queueStatusResponse.setTokenId(tokenId);
            queueStatusResponse.setStatus("NOT_IN_QUEUE");
            queueStatusResponse.setExpiresAt(null);
            queueStatusResponse.setQueuePosition(null);
        }

        return queueStatusResponse;
    }
}
