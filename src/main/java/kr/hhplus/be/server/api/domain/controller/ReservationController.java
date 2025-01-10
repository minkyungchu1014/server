package kr.hhplus.be.server.api.domain.controller;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.api.domain.dto.AvailableDateResponse;
import kr.hhplus.be.server.api.domain.dto.AvailableSeatsResponse;
import kr.hhplus.be.server.api.domain.usecase.ReservationFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reserveation")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    /**
     * 예약 가능 날짜와 해당 날짜의 콘서트 및 스케줄 목록을 반환합니다.
     * @return 예약 가능한 날짜와 콘서트 목록 리스트
     */
    @GetMapping("/dates")
    public ResponseEntity<List<AvailableDateResponse>> getAvailableDates() {
        return ResponseEntity.ok(reservationFacade.getAvailableDates());
    }

    /**
     * 좌석 정보 조회
     * @param date 조회할 콘서트 스케줄 ID
     * @return 예약 가능한 좌석 정보 리스트
     */
    @GetMapping("/seats")
    public ResponseEntity<List<AvailableSeatsResponse>> getAvailableSeatsByDate(@RequestParam String date) {
        return ResponseEntity.ok(reservationFacade.getAvailableSeatsByDate(date));
    }


    /**
     * 좌석 예약 요청
     * @param userId 사용자 ID
     * @param seatId 좌석 ID
     * @return 예약 성공 메시지
     */
    @PostMapping("/reserve")
    public ResponseEntity<String> reserveSeat(@RequestParam Long userId, @RequestParam Long seatId) {
        try {
            reservationFacade.reserveSeat(userId, seatId);
            return ResponseEntity.ok("예약 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예약 오류" + e.getMessage());
        }
    }



}
