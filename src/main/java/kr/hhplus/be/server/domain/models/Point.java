package kr.hhplus.be.server.domain.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Point: 사용자별 포인트 잔액 관리 엔티티.
 */
@Getter
@Setter
@Entity
@Table(name = "point")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // 각 사용자와 고유하게 연결된 Point 계정

    @Column(nullable = false)
    private Long total = 0L; // 총 포인트 초기값을 0으로 설정

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 레코드 생성 시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 마지막 수정 시간


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
