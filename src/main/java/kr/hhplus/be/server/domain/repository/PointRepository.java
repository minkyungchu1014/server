package kr.hhplus.be.server.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.models.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PointRepository
 * - JPA를 사용한 포인트 데이터 관리.
 */
@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    // 비관적 락을 사용하여 특정 사용자의 포인트 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId")
    Optional<Point> findPointWithLock(@Param("userId") Long userId);

    // 특정 사용자의 현재 포인트 잔액을 조회
    @Query("SELECT p.total FROM Point p WHERE p.user.id = :userId")
    Long getPoint(@Param("userId") Long userId);

    // 포인트 거래 기록 추가
    @Query(value = "INSERT INTO point_history (user_id, amount, type, description, created_at) " +
            "VALUES (:userId, :amount, :type, :description, :createdAt)", nativeQuery = true)
    void addPointHistory(@Param("userId") Long userId,
                         @Param("amount") Long amount,
                         @Param("type") String type,
                         @Param("description") String description,
                         @Param("createdAt") LocalDateTime createdAt);

}
