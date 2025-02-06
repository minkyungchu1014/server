package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {


    List<PointHistory> findByUserId(Long userId);
}
