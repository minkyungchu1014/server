package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
