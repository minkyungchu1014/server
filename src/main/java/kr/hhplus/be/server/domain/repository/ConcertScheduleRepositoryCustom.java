package kr.hhplus.be.server.domain.repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ConcertScheduleRepositoryCustom
 * - 커스텀 쿼리를 정의하는 인터페이스.
 */
public interface ConcertScheduleRepositoryCustom {

    /**
     * 특정 날짜에 해당하는 스케줄 ID 조회.
     * @param scheduleDate 스케줄 날짜
     * @return 스케줄 ID 리스트
     */
    List<Long> findScheduleIdsByDate(LocalDate scheduleDate);
}
