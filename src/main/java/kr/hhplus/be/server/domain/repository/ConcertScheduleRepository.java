package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    @Query("SELECT cs FROM ConcertSchedule cs WHERE cs.startDatetime > CURRENT_TIMESTAMP")
    List<ConcertSchedule> findAvailableSchedules();

    /**
     * 특정 날짜의 스케줄 ID 조회
     * @param date 날짜 (bsdt 기준)
     * @return 해당 날짜의 스케줄 ID 리스트
     */
    @Query("SELECT cs.id FROM ConcertSchedule cs WHERE DATE(cs.startDatetime) = :date")
    List<Long> findScheduleIdsByDate(@Param("date") LocalDate date);

    /**
     * 특정 날짜에 해당하는 스케줄 ID 조회.
     * @param scheduleDate 스케줄 날짜
     * @return 스케줄 ID 리스트
     */
    List<Long> findIdsByScheduleDate(LocalDate scheduleDate);
}
