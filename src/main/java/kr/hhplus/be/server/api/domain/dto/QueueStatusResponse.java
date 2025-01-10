package kr.hhplus.be.server.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * QueueStatusResponse
 * - 대기열 상태 정보를 클라이언트에 전달하기 위한 DTO 클래스.
 */
@Getter
@Setter
public class QueueStatusResponse {
    private String tokenId; // 토큰 ID
    private String status; // 대기열 상태 (WAITING, ACTIVE, etc.)
    private Long queuePosition; // 대기 순번
    private LocalDateTime expiresAt; // 만료 시간
}
