package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.repository.SeatRepository;
import org.springframework.stereotype.Service;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public boolean isSeatAvailable(Long seatId) {
        return seatRepository.isSeatAvailable(seatId);
    }

    public Long getSeatPrice(Long seatId) {
        return seatRepository.getSeatPrice(seatId);
    }

    public void markSeatAsReserved(Long seatId, Long userId) {
        seatRepository.updateSeatStatus(seatId, true, userId);
    }

    public void markSeatAsAvailable(Long seatId) {
        seatRepository.updateSeatStatus(seatId, false, null);
    }
}
