package kr.hhplus.be.server.interfaces.api;
import kr.hhplus.be.server.domain.models.Concert;
import kr.hhplus.be.server.domain.models.ConcertSchedule;
import kr.hhplus.be.server.domain.models.Reservation;
import kr.hhplus.be.server.domain.models.Seat;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import kr.hhplus.be.server.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.domain.repository.ReservationRepository;
import kr.hhplus.be.server.domain.repository.SeatRepository;
import kr.hhplus.be.server.domain.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class ReservationServiceIntegrationTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @Mock
    private SeatRepository seatRepository;

    @Test
    @Transactional
    public void testGetAvailableConcertSchedules() {
        List<ConcertSchedule> result = reservationService.getAvailableConcertSchedules();
        assertNotNull(result);
    }

    @Test
    @Transactional
    public void testGetAvailableSeats() {
        // 고정된 시간 설정
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-09T21:03:44Z"), ZoneId.systemDefault());
        LocalDateTime fixedTime = LocalDateTime.now(fixedClock);
        Reservation reservation = new Reservation();
        reservation.setStatus("PENDING");
        reservation.setExpiresAt(fixedTime.minusMinutes(1));
        reservationRepository.save(reservation);

        List<Long> result = reservationService.getAvailableSeats(fixedTime.toString());

        assertEquals(1, result.size());
//        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    @Transactional
    public void testGetConcertById() {
        Concert concert = new Concert();
        concertRepository.save(concert);

        Concert result = reservationService.getConcertById(concert.getId());
        assertNotNull(result);
    }


    @Test
    @Transactional
    public void testGetAvailableSeatsByDate() {
        // 고정된 시간 설정
        Clock fixedClock = Clock.fixed(Instant.parse("2025-01-09T21:03:44Z"), ZoneId.systemDefault());
        LocalDateTime fixedTime = LocalDateTime.now(fixedClock);
        ConcertSchedule schedule = new ConcertSchedule();
        schedule.setScheduleDate(LocalDate.from(fixedTime));
        concertScheduleRepository.save(schedule);

        Seat seat = new Seat();
        seat.setConcertScheduleId(schedule.getId());
        seatRepository.save(seat);

        List<Seat> result = reservationService.getAvailableSeatsByDate(fixedTime.toString());
        assertNotNull(result);
    }

    @Test
    @Transactional
    public void testGetAvailableDates() {
        List<String> result = reservationService.getAvailableDates();
        assertNotNull(result);
    }


    @Test
    public void testReserveSeat() {
        Long userId = 1L;
        Long seatId = 1L;

        reservationService.reserveSeat(userId, seatId);

        verify(reservationRepository, times(1)).reserveSeat(userId, seatId);
    }

    @Test
    public void testGetSeatIdByReservation() {
        Long reservationId = 1L;
        Long seatId = 1L;
        when(reservationRepository.findSeatIdById(reservationId)).thenReturn(seatId);

        Long result = reservationService.getSeatIdByReservation(reservationId);

        assertEquals(seatId, result);
    }

    @Test
    public void testUpdateReservationStatus() {
        Long reservationId = 1L;
        String status = "CONFIRMED";

        reservationService.updateReservationStatus(reservationId, status);

        verify(reservationRepository, times(1)).updateReservationStatus(reservationId, status);
    }

    @Test
    public void testProcessExpiredReservations() {
        List<Reservation> expiredReservations = Collections.singletonList(new Reservation());
        when(reservationRepository.findByStatusAndExpiresAtBefore(eq("PENDING"), any())).thenReturn(expiredReservations);

        reservationService.processExpiredReservations();

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(seatRepository, times(1)).updateSeatStatus(anyLong(), eq(false), isNull());
    }

}