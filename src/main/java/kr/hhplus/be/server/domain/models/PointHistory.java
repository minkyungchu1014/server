package kr.hhplus.be.server.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * PointHistory: 포인트 변경 내역 기록 엔티티.
 */
@Getter
@Setter
@Entity
@Table(name = "point_history")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId; // 각 PointHistory는 Point와 연결됨

    @Column(nullable = false)
    private Long amount; // 변경된 포인트 금액 (양수 또는 음수)

    @Column(nullable = false, length = 50)
    private String type; // 거래 유형 (예: "ADD", "DEDUCT")

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 거래 발생 시간

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
}