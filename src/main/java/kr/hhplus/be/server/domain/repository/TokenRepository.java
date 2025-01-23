package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long>, TokenRepositoryCustom {
    boolean existsByUserId(Long userId);
    boolean existsByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime now);
    void deleteByUserId(Long userId);


    @Query("SELECT CASE WHEN t.expiresAt <= CURRENT_TIMESTAMP THEN true ELSE false END FROM Token t WHERE t.token = :token")
    boolean isTokenExpired(@Param("token") String token);

}
