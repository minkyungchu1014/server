package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReservationIntegrationTest {

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
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
    }

    @Test
    void testReserveSeat_Success() {
        // Arrange
        Long userId = 1L;
        Long seatId = 1L;

        // Act
        ResponseEntity<Reservation> response = restTemplate.postForEntity(
                "/api/reserveation/reserve?userId=" + userId + "&seatId=" + seatId, null, Reservation.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Optional<Reservation> reservation = reservationRepository.findBySeatId(seatId);
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getStatus()).isEqualTo("RESERVED");
    }

    @Test
    void testCancelReservation_Success() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setSeatId(1L);
        reservation.setStatus("RESERVED");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        reservationRepository.save(reservation);

        // Act
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/reserveation/cancel?reservationId=" + reservation.getId(), null, Void.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Optional<Reservation> updatedReservation = reservationRepository.findById(reservation.getId());
        assertThat(updatedReservation).isPresent();
        assertThat(updatedReservation.get().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void testGetAvailableSeatsByDate_Success() {
        // Arrange
        String date = "2023-01-01";

        // Act
        ResponseEntity<Seat[]> response = restTemplate.getForEntity(
                "/api/reserveation/seats?date=" + date, Seat[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testProcessExpiredReservations() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setSeatId(1L);
        reservation.setStatus("PENDING");
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        reservationRepository.save(reservation);

        // Act
        restTemplate.postForEntity("/api/reserveation/process-expired", null, Void.class);

        // Assert
        Optional<Reservation> updatedReservation = reservationRepository.findById(reservation.getId());
        assertThat(updatedReservation).isPresent();
        assertThat(updatedReservation.get().getStatus()).isEqualTo("EXPIRED");
    }
}
