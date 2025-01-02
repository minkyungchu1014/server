package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.service.ReservationService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

}
