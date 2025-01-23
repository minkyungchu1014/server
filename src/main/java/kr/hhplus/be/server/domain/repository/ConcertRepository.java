package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {

    @Query("SELECT cs.concertId FROM ConcertSchedule cs WHERE cs.id = :scheduleId")
    Long findConcertByScheduleId(@Param("scheduleId") Long scheduleId);


}
