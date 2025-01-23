package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PaymentRepository
 * - 결제 데이터를 관리하는 JPA Repository 인터페이스.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

    /**
     * 예약 ID로 결제 정보를 조회.
     * @param reservationId 예약 ID
     * @return 해당 예약 ID에 연결된 결제 정보(Optional)
     */
    @Query("SELECT p FROM Payment p WHERE p.reservationId = :reservationId")
    Optional<Payment> findByReservationId(@Param("reservationId") Long reservationId);

    /**
     * 특정 예약 ID와 결제 상태로 데이터가 존재하는지 확인.
     * @param reservationId 예약 ID
     * @param status 결제 상태
     * @return 데이터 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.reservationId = :reservationId AND p.status = :status")
    boolean existsByReservationIdAndStatus(@Param("reservationId") Long reservationId, @Param("status") String status);

}
