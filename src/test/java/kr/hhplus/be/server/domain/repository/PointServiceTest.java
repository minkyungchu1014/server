package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.models.Point;
import kr.hhplus.be.server.domain.models.PointHistory;
import kr.hhplus.be.server.domain.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeductPoint_Success() {
        // Arrange
        Long userId = 1L;
        Long amount = 100L;
        String description = "테스트 차감";

        // Mock Point 객체 생성 및 초기화
        Point mockPoint = new Point();
        mockPoint.setTotal(200L); // 초기 값 설정

        when(pointRepository.findPointWithLock(userId)).thenReturn(Optional.of(mockPoint));

        // Act
        pointService.deductPoint(userId, amount, description);

        // Assert
        assertEquals(100L, mockPoint.getTotal()); // 남은 포인트가 100인지 확인
        verify(pointRepository, times(1)).save(mockPoint);
        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
    }


    @Test
    void testDeductPoint_InsufficientBalance() {
        Long userId = 1L;
        Long amount = 300L;
        Point mockPoint = new Point();

        when(pointRepository.findPointWithLock(userId)).thenReturn(Optional.of(mockPoint));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.deductPoint(userId, amount, "테스트"));

        assertEquals("포인트가 부족합니다.", exception.getMessage());
        verify(pointRepository, never()).save(any());
    }

    @Test
    void testAddPoint_Success() {
        // Arrange
        Long userId = 1L;
        Long amount = 100L;
        String description = "테스트 추가";
        Point mockPoint = new Point();
        mockPoint.setTotal(200L); // 초기 값 설정

        when(pointRepository.findPointWithLock(userId)).thenReturn(Optional.of(mockPoint));

        // Act
        pointService.addPoint(userId, amount, description);

        // Assert
        assertEquals(300L, mockPoint.getTotal()); // total 값이 300인지 확인
        verify(pointRepository, times(1)).save(mockPoint);
        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
    }

    @Test
    void testGetPoint() {
        Long userId = 1L;
        Long expectedTotal = 500L;

        when(pointRepository.getPoint(userId)).thenReturn(expectedTotal);

        Long total = pointService.getPoint(userId);

        assertEquals(expectedTotal, total);
        verify(pointRepository, times(1)).getPoint(userId);
    }
}
