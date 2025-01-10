package kr.hhplus.be.server.interfaces.api;

import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class PointServiceIntegrationTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @Test
    @Transactional
    public void testGetPoint() {
        Long userId = 1L;
        pointRepository.updatePoint(userId, 100L);

        Long points = pointService.getPoint(userId);

        assertEquals(100L, points);
    }

    @Test
    @Transactional
    public void testDeductPoint() {
        Long userId = 1L;
        pointRepository.updatePoint(userId, 100L);

        pointService.deductPoint(userId, 50L, "Test Deduction");

        Long points = pointService.getPoint(userId);
        assertEquals(50L, points);
    }

    @Test
    @Transactional
    public void testAddPoint() {
        Long userId = 1L;
        pointRepository.updatePoint(userId, 100L);

        pointService.addPoint(userId, 50L, "Test Addition");

        Long points = pointService.getPoint(userId);
        assertEquals(150L, points);
    }
}