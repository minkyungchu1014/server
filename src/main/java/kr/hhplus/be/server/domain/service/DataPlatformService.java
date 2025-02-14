package kr.hhplus.be.server.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class DataPlatformService {
    private static final Logger logger = LoggerFactory.getLogger(DataPlatformService.class);
    private final RestTemplate restTemplate;
    private static final String DATA_PLATFORM_URL = "https://mock-dataplatform.com/api/reservations";

    public DataPlatformService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendReservationData(Long reservationId, Long userId, Long seatId, Long concertScheduleId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reservationId", reservationId);
        requestBody.put("userId", userId);
        requestBody.put("seatId", seatId);
        requestBody.put("concertScheduleId", concertScheduleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    DATA_PLATFORM_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            logger.info("데이터 플랫폼 응답: {}", response.getBody());
        } catch (Exception e) {
            logger.error("데이터 플랫폼 전송 실패: {}", e.getMessage());
        }
    }
}
