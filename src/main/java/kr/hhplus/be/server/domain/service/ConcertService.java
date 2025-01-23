package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.models.Concert;
import kr.hhplus.be.server.domain.repository.ConcertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepository;

    public Concert getConcertByScheduleId(Long concertScheduleId) {
        Long concertId = concertRepository.findConcertByScheduleId(concertScheduleId);
        return concertRepository.findById(concertId).orElseThrow(() -> new IllegalArgumentException("Invalid concert ID: " + concertId));
    }
}