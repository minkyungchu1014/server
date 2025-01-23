package kr.hhplus.be.server.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ConcertSchedule: 콘서트 스케줄 관리 엔티티.
 */
@Getter
@Setter
@Entity
@Table(name = "concert_schedule")
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "concert_id", nullable = false)
    private Long concertId; // Concert와 1:N 관계

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate; // 스케줄 날짜

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime; // 스케줄 시작 시간

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime; // 스케줄 종료 시간

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성 시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 시간

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
