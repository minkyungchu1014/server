package kr.hhplus.be.server.domain.repository;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import kr.hhplus.be.server.domain.models.Concert;
import kr.hhplus.be.server.domain.models.ConcertSchedule;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class ReservationServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAvailableSeats() {
        String date = "2023-12-25";
        LocalDate reservationDate = LocalDate.parse(date);
        List<Long> expectedSeats = Arrays.asList(1L, 2L, 3L);

        when(seatRepository.findIdByDate(reservationDate)).thenReturn(expectedSeats);

        List<Long> availableSeats = reservationService.getAvailableSeats(date);

        assertEquals(expectedSeats, availableSeats);
        verify(seatRepository, times(1)).findIdByDate(reservationDate);
    }

    @Test
    public void testGetAvailableConcertSchedules() {
        List<ConcertSchedule> expectedSchedules = Arrays.asList(new ConcertSchedule(), new ConcertSchedule());
        when(concertScheduleRepository.findAvailableSchedules()).thenReturn(expectedSchedules);

        List<ConcertSchedule> result = reservationService.getAvailableConcertSchedules();

        assertEquals(expectedSchedules, result);
        verify(concertScheduleRepository, times(1)).findAvailableSchedules();
    }

    @Test
    public void testReserveSeat() {
        Long userId = 1L;
        Long seatId = 2L;

        doNothing().when(reservationRepository).reserveSeat(userId, seatId);

        reservationService.reserveSeat(userId, seatId);

        verify(reservationRepository, times(1)).reserveSeat(userId, seatId);
    }

    @Test
    public void testGetConcertById() {
        Long concertId = 1L;
        Concert expectedConcert = new Concert();
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(expectedConcert));

        Concert result = reservationService.getConcertById(concertId);

        assertEquals(expectedConcert, result);
        verify(concertRepository, times(1)).findById(concertId);
    }

    @Test
    public void testGetAvailableSeatsByDate() {
        String date = "2023-12-25";
        LocalDate bsdtDate = LocalDate.parse(date);
        List<Long> scheduleIds = Arrays.asList(1L, 2L);
        List<Seat> expectedSeats = Arrays.asList(new Seat(), new Seat());

        when(concertScheduleRepository.findScheduleIdsByDate(bsdtDate)).thenReturn(scheduleIds);
        when(seatRepository.findAvailableSeatsByScheduleIds(scheduleIds)).thenReturn(expectedSeats);

        List<Seat> result = reservationService.getAvailableSeatsByDate(date);

        assertEquals(expectedSeats, result);
        verify(concertScheduleRepository, times(1)).findScheduleIdsByDate(bsdtDate);
        verify(seatRepository, times(1)).findAvailableSeatsByScheduleIds(scheduleIds);
    }

    @Test
    public void testGetAvailableDates() {
        List<LocalDate> availableDates = Arrays.asList(LocalDate.now(), LocalDate.now().plusDays(1));
        when(reservationRepository.findAvailableDates()).thenReturn(availableDates);

        List<String> result = reservationService.getAvailableDates();

        assertEquals(availableDates.size(), result.size());
        verify(reservationRepository, times(1)).findAvailableDates();
    }

    @Test
    public void testGetSeatIdByReservation() {
        Long reservationId = 1L;
        Long expectedSeatId = 2L;

        when(reservationRepository.findSeatIdById(reservationId)).thenReturn(expectedSeatId);

        Long seatId = reservationService.getSeatIdByReservation(reservationId);

        assertEquals(expectedSeatId, seatId);
        verify(reservationRepository, times(1)).findSeatIdById(reservationId);
    }

    @Test
    public void testUpdateReservationStatus() {
        Long reservationId = 1L;
        String status = "CONFIRMED";

        doNothing().when(reservationRepository).updateReservationStatus(reservationId, status);

        reservationService.updateReservationStatus(reservationId, status);

        verify(reservationRepository, times(1)).updateReservationStatus(reservationId, status);
    }

    @Test
    public void testProcessExpiredReservations() {
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setSeatId(1L);
        reservation1.setStatus("PENDING");
        reservation1.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setSeatId(2L);
        reservation2.setStatus("PENDING");
        reservation2.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        List<Reservation> expiredReservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findByStatusAndExpiresAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(expiredReservations);

        reservationService.processExpiredReservations();

        verify(reservationRepository, times(1)).findByStatusAndExpiresAtBefore(eq("PENDING"), any(LocalDateTime.class));
        verify(reservationRepository, times(2)).save(any(Reservation.class));
        verify(seatRepository, times(2)).updateSeatStatus(anyLong(), eq(false), isNull());
    }
}