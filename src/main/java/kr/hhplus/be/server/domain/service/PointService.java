package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.api.domain.dto.TransactionHistoryResponse;
import kr.hhplus.be.server.domain.repository.PointRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointService {

    private final PointRepository pointRepository;

    // 생성자 주입을 통해 PointRepository를 제공받음
    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    /**
     * 사용자의 현재 포인트를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 사용자의 현재 포인트 잔액
     */
    public Long getPoint(Long userId) {
        return pointRepository.getPoint(userId);
    }

    /**
     * 사용자의 포인트를 차감합니다.
     * @param userId 포인트를 차감할 사용자 ID
     * @param amount 차감할 금액 (0보다 커야 함)
     * @param description 거래 설명
     * @throws IllegalArgumentException 금액이 잘못되었거나 포인트가 부족한 경우 예외를 발생시킴
     */
    public void deductPoint(Long userId, Long amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }
        Long currentPoint = pointRepository.getPoint(userId);
        if (currentPoint < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        pointRepository.updatePoint(userId, currentPoint - amount);
        pointRepository.addPointHistory(userId, -amount, "DEDUCT", description);
    }

    /**
     * 사용자의 포인트를 추가합니다.
     * @param userId 포인트를 추가할 사용자 ID
     * @param amount 추가할 금액 (0보다 커야 함)
     * @param description 거래 설명
     * @throws IllegalArgumentException 금액이 잘못된 경우 예외를 발생시킴
     */
    public void addPoint(Long userId, Long amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("추가 금액은 0보다 커야 합니다.");
        }
        Long currentPoint = pointRepository.getPoint(userId);
        pointRepository.updatePoint(userId, currentPoint + amount);
        pointRepository.addPointHistory(userId, amount, "ADD", description);
    }

}
