package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Point;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

public class PointIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("password1!");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    private final Long userId = 1L;

    @BeforeEach
    public void setUp() {
        // 테스트 데이터를 초기화
        pointRepository.deleteAll();
        Point point = new Point();
        point.setId(userId);
        point.setTotal(100L); // 초기 포인트 설정
        pointRepository.save(point);
    }

    @Test
    void testGetPoint_Success() {
        // Arrange: API 호출 준비

        // Act: API 호출
        ResponseEntity<Long> response = restTemplate.getForEntity("/api/point/" + userId, Long.class);

        // Assert: 응답 상태와 결과 검증
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(100L);
    }

    @Test
    void testAddPoint_Success() {
        // Arrange: 추가할 포인트 데이터 준비
        Long amountToAdd = 50L;

        // Act: API 호출로 포인트 추가 요청
        restTemplate.postForEntity("/api/point/" + userId + "/add", amountToAdd, Void.class);

        // Assert: 포인트가 정상적으로 추가되었는지 확인
        Optional<Point> pointOptional = pointRepository.findPointWithLock(userId);
        assertThat(pointOptional).isPresent();
        assertThat(pointOptional.get().getTotal()).isEqualTo(150L);
    }

    @Test
    void testDeductPoint_Success() {
        // Arrange: 차감할 포인트 데이터 준비
        Long amountToDeduct = 50L;

        // Act: 서비스 메서드를 통해 포인트 차감
        pointService.deductPoint(userId, amountToDeduct, "테스트 차감");

        // Assert: 포인트가 정상적으로 차감되었는지 확인
        Optional<Point> pointOptional = pointRepository.findPointWithLock(userId);
        assertThat(pointOptional).isPresent();
        assertThat(pointOptional.get().getTotal()).isEqualTo(50L);
    }

    @Test
    void testDeductPoint_Failure_InsufficientPoints() {
        // Arrange: 부족한 포인트를 차감 시도
        Long amountToDeduct = 200L;

        // Act & Assert: 예외가 발생해야 함
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/point/" + userId + "/deduct", amountToDeduct, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("포인트가 부족합니다.");
    }
}
