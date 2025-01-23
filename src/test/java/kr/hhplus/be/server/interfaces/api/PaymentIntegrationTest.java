package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.api.domain.dto.PaymentRequest;
import kr.hhplus.be.server.api.domain.usecase.PaymentFacade;
import kr.hhplus.be.server.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("root")
            .withPassword("password1!")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @MockBean
    private PaymentFacade paymentFacade;

    @Autowired
    private PaymentRepository paymentRepository;

    private final Long userId = 1L;
    private final Long reservationId = 1L;

    @BeforeEach
    public void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentRequest paymentRequest = new PaymentRequest(userId, reservationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);

        String url = "http://localhost:" + port + "/api/payment/process";

        // Mock 설정
        when(paymentFacade.processPayment(userId, reservationId)).thenReturn("결제 성공");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("결제 성공");
    }
}
