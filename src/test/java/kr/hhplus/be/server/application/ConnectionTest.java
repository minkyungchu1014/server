package kr.hhplus.be.server.application;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.models.Payment;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
public class ConnectionTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActiveDatabase() {
        String activeDatabase = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        System.out.println("Active Database: " + activeDatabase);
    }

    @Test
    void testContainerIsRunning() {
        assertTrue(mysqlContainer.isRunning(), "MySQL container is not running!");
    }

    @Test
    @Transactional
    public void testUpdatePaymentStatusWithJPQL() {
        Payment payment = new Payment();
        payment.setUserId(1L);
        payment.setReservationId(1L);
        payment.setAmount(10000L);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.parse("2024-08-01T00:00"));
        payment.setUpdatedAt(LocalDateTime.parse("2024-08-01T00:00"));
        paymentRepository.save(payment);

        // Act
        int updatedCount = paymentRepository.updatePaymentStatus("COMPLETE", 1L);

        // Assert
        assertThat(updatedCount).isEqualTo(1);
        assertThat(paymentRepository.findById(payment.getId()).get().getStatus()).isEqualTo("COMPLETE");
    }



}