package kr.hhplus.be.server.api.domain.controller;

import kr.hhplus.be.server.api.domain.usecase.ReservationFacade;
import kr.hhplus.be.server.domain.service.DataPlatformService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationFacade reservationFacade;
    private final DataPlatformService dataPlatformService;

    public ReservationController(ReservationFacade reservationFacade, DataPlatformService dataPlatformService) {
        this.reservationFacade = reservationFacade;
        this.dataPlatformService = dataPlatformService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveSeat(
            @RequestHeader("Authorization") String token,
            @RequestParam Long userId,
            @RequestParam Long seatId
    ) {
        try {
            reservationFacade.reserveSeat(token, userId, seatId);
            return ResponseEntity.ok("예약 성공 및 데이터 플랫폼 전송 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예약 오류: " + e.getMessage());
        }
    }
}
