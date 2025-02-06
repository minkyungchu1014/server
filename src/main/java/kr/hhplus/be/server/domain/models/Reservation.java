package kr.hhplus.be.server.domain.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "reservation")
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @JoinColumn(name = "seat_id", nullable = false)
    private Long seatId;

    @JoinColumn(name = "concert_schedule_id", nullable = false)
    private Long concertScheduleId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Reservation(Long userId) {
        this.userId = userId;
        this.seatId = 1L;  // 기본값 설정
        this.concertScheduleId = 1L;  // 기본값 설정
        this.status = "PENDING";  // 기본 상태 설정
        this.expiresAt = LocalDateTime.now().plusMinutes(10);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Reservation(Long userId, Long seatId, Long concertScheduleId, String status, LocalDateTime expiresAt) {
        this.userId = userId;
        this.seatId = seatId;
        this.concertScheduleId = concertScheduleId;
        this.status = status;
        this.expiresAt = expiresAt;
    }

}
