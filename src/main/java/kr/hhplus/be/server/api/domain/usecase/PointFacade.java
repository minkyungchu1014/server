package kr.hhplus.be.server.api.domain.usecase;

import kr.hhplus.be.server.domain.service.PointService;
import org.springframework.stereotype.Component;

/**
 * PointFacade
 * - 포인트와 관련된 비즈니스 로직을 캡슐화하여 제공하는 클래스.
 * - Service 계층을 조합하여 포인트 관리와 관련된 고수준의 작업을 처리.
 */
@Component
public class PointFacade {

    private final PointService pointService;

    // 생성자 주입을 통해 PointService를 제공받음
    public PointFacade(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * 사용자의 포인트를 충전합니다.
     * @param userId 포인트를 충전할 사용자 ID
     * @param amount 충전할 포인트 금액 (0보다 커야 함)
     * @throws IllegalArgumentException 금액이 0 이하일 경우 예외를 발생시킴
     */
    public void rechargePoint(Long userId, Long amount) {
        pointService.addPoint(userId, amount, "충전");
    }

    /**
     * 사용자의 현재 포인트 잔액을 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 사용자의 현재 포인트 잔액
     */
    public Long getPoint(Long userId) {
        return pointService.getPoint(userId);
    }

    public void deductPoint(Long userId, Long amount) {
        pointService.deductPoint(userId, amount, "포인트 사용");
    }

}
