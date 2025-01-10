package kr.hhplus.be.server.api.domain.controller;

import kr.hhplus.be.server.api.domain.dto.QueueStatusResponse;
import kr.hhplus.be.server.api.domain.usecase.TokenFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * QueueController
 * - 대기열 상태를 확인하는 컨트롤러.
 * - Facade 계층(TokenFacade)을 통해 비즈니스 로직을 처리.
 */
@RestController
public class QueueController {

    private final TokenFacade tokenFacade;

    public QueueController(TokenFacade tokenFacade) {
        this.tokenFacade = tokenFacade;
    }

    /**
     * 대기열 상태 확인 API
     * @param token Authorization 헤더에 포함된 토큰 값
     * @return QueueStatusResponse (대기열 상태)
     */
    @GetMapping("/status")
    public QueueStatusResponse checkQueueStatus(@RequestHeader("Authorization") String token) {
        // TokenFacade를 통해 대기열 상태 확인
        return tokenFacade.getQueueStatus(token);
    }
}
