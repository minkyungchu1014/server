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
