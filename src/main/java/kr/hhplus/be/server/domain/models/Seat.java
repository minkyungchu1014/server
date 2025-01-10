package kr.hhplus.be.server.domain.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Seats: 콘서트 좌석 관리 엔티티.
 */
@Getter
@Setter
@Entity
@Table(name = "seat")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "concert_schedule_id", nullable = false)
    private Long concertScheduleId; // ConcertSchedule과 1:N 관계

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber; // 좌석 번호 1 ~ 50

    @Column(nullable = false)
    private Long price; // 좌석 가격

    @Column(nullable = false)
    private Boolean isReserved; // 예약 여부

    @JoinColumn(name = "reserved_by")
    private Long reservedBy; // 활성화된 예약 사용자 (nullable)

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

    // Getters and Setters omitted for brevity
}
