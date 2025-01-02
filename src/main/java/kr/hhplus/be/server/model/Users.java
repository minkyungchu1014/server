package kr.hhplus.be.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;  // 사용자 고유 UUID

    @Column(nullable = false)
    private String name;  // 사용자 이름

    @Column(nullable = false)
    private Long point;  // 포인트 또는 잔액

    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간
}
