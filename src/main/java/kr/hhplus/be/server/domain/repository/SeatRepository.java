package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, SeatRepositoryCustom {

    @Query("SELECT s FROM Seat s WHERE s.concertScheduleId IN :scheduleIds AND s.isReserved = FALSE")
    List<Seat> findAvailableSeatsByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    Long getSeatPrice(Long seatId);


    @Modifying
    @Query("UPDATE Seat s SET s.isReserved = FALSE, s.reservedBy = NULL WHERE s.reservedBy = :userId")
    void updateIsReservedFalseAndReservedByEmptyByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Seat s SET s.isReserved = TRUE, s.reservedBy = :userId WHERE s.id = :id")
    void updateIsReservedTrueAndReservedBy(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT s.id FROM Seat s WHERE s.concertScheduleId IN (SELECT cs.id FROM ConcertSchedule cs WHERE cs.scheduleDate = :date)")
    List<Long> findIdByDate(@Param("date") LocalDate date);
}
