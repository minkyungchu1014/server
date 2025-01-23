package kr.hhplus.be.server.domain.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.domain.repository.ConcertScheduleRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ConcertScheduleRepositoryImpl
 * - 커스텀 로직을 구현한 클래스.
 */
@Repository
public class ConcertScheduleRepositoryImpl implements ConcertScheduleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Long> findScheduleIdsByDate(LocalDate scheduleDate) {
        String query = """
            SELECT cs.id
            FROM ConcertSchedule cs
            WHERE cs.scheduleDate = :scheduleDate
        """;

        return entityManager.createQuery(query, Long.class)
                .setParameter("scheduleDate", scheduleDate)
                .getResultList();
    }
}
