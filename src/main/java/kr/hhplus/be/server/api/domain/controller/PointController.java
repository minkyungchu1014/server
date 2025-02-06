package kr.hhplus.be.server.api.domain.controller;

import kr.hhplus.be.server.api.domain.dto.PointRequest;
import kr.hhplus.be.server.api.domain.dto.PointResponse;
import kr.hhplus.be.server.api.domain.usecase.PointFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/point")
public class PointController {

    private final PointFacade pointFacade;

    public PointController(PointFacade pointFacade) {
        this.pointFacade = pointFacade;
    }

    /**
     * 사용자의 현재 포인트를 조회합니다.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<PointResponse> getUserPoints(@PathVariable Long userId) {
        Long points = pointFacade.getPoint(userId);
        return ResponseEntity.ok(new PointResponse("현재 포인트 조회 성공", points));
    }

    /**
     * 사용자의 포인트를 추가합니다.
     */
    @PostMapping("/{userId}/add")
    public ResponseEntity<PointResponse> addPoints(@PathVariable Long userId, @RequestBody PointRequest request) {
        if (request.getAmount() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PointResponse("포인트 금액은 0보다 커야 합니다."));
        }

        try {
            pointFacade.rechargePoint(userId, request.getAmount());
            return ResponseEntity.ok(new PointResponse("포인트가 성공적으로 추가되었습니다.", pointFacade.getPoint(userId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PointResponse("포인트 추가 실패: " + e.getMessage()));
        }
    }

    /**
     * 사용자의 포인트를 차감합니다.
     */
    @PostMapping("/{userId}/deduct")
    public ResponseEntity<PointResponse> deductPoints(@PathVariable Long userId, @RequestBody PointRequest request) {
        if (request.getAmount() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PointResponse("포인트 차감 금액은 0보다 커야 합니다."));
        }

        try {
            pointFacade.deductPoint(userId, request.getAmount());
            return ResponseEntity.ok(new PointResponse("포인트가 성공적으로 차감되었습니다.", pointFacade.getPoint(userId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PointResponse("포인트 차감 실패: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PointResponse("서버 오류: " + e.getMessage()));
        }
    }
}
