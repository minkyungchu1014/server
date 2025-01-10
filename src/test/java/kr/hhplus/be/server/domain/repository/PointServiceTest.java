package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPoint() {
        Long userId = 1L;
        when(pointRepository.getPoint(userId)).thenReturn(100L);

        Long points = pointService.getPoint(userId);

        assertEquals(100L, points);
    }

    @Test
    public void testDeductPoint() {
        Long userId = 1L;
        when(pointRepository.getPoint(userId)).thenReturn(100L);

        pointService.deductPoint(userId, 50L, "Test Deduction");

        verify(pointRepository, times(1)).updatePoint(userId, 50L);
        verify(pointRepository, times(1)).addPointHistory(userId, -50L, "DEDUCT", "Test Deduction");
    }

    @Test
    public void testAddPoint() {
        Long userId = 1L;
        when(pointRepository.getPoint(userId)).thenReturn(100L);

        pointService.addPoint(userId, 50L, "Test Addition");

        verify(pointRepository, times(1)).updatePoint(userId, 150L);
        verify(pointRepository, times(1)).addPointHistory(userId, 50L, "ADD", "Test Addition");
    }
}