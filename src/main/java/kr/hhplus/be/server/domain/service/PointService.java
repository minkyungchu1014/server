package kr.hhplus.be.server.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.models.Point;
import kr.hhplus.be.server.domain.models.PointHistory;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointService(PointRepository pointRepository, PointHistoryRepository pointHistoryRepository) {
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }


    @Transactional
    public void deductPoint(Long userId, Long amount, String description) {
        // 1. 비관적 락을 사용하여 포인트 조회
        Point point = pointRepository.findPointWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자의 포인트 정보가 없습니다."));

        // 2. 포인트가 부족한 경우 예외 발생
        if (point.getTotal() < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // 3. 포인트 차감 및 저장
        point.setTotal(point.getTotal() - amount);
        pointRepository.save(point);

        // 4. 포인트 히스토리 저장
        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setAmount(-amount);
        history.setType("DEDUCT");
        history.setCreatedAt(LocalDateTime.now());
        pointHistoryRepository.save(history);
    }

    /**
     * 사용자의 포인트를 추가합니다.
     * @param userId 포인트를 추가할 사용자 ID
     * @param amount 추가할 금액 (0보다 커야 함)
     * @param description 거래 설명
     * @throws IllegalArgumentException 금액이 잘못된 경우 예외를 발생시킴
     */
    @Transactional
    public void addPoint(Long userId, Long amount, String description) {
        // 1. 비관적 락을 사용하여 포인트 조회
        Point point = pointRepository.findPointWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자의 포인트 정보가 없습니다."));

        // 2. 포인트 추가 및 저장
        point.setTotal(point.getTotal() + amount);
        pointRepository.save(point);

        // 3. 포인트 히스토리 저장
        PointHistory history = new PointHistory();
        history.setUserId(userId);
        history.setAmount(amount);
        history.setType("ADD");
        history.setCreatedAt(LocalDateTime.now());
        pointHistoryRepository.save(history);
    }


    /**
     * 사용자의 현재 포인트를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 사용자의 현재 포인트 잔액
     */
    public Long getPoint(Long userId) {
        return pointRepository.getPoint(userId);
    }



}
