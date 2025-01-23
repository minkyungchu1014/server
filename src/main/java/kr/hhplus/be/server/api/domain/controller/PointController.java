package kr.hhplus.be.server.api.domain.controller;

import kr.hhplus.be.server.api.domain.dto.*;
import kr.hhplus.be.server.api.domain.usecase.PointFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/point")
public class PointController {

    private final PointFacade pointFacade;

    public PointController(PointFacade pointFacade) {
        this.pointFacade = pointFacade;
    }

    /**
     * 사용자의 현재 포인트를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자의 현재 포인트 잔액
     */
    @GetMapping("/{userId}")
    public ResponseEntity<PointResponse> getUserPoints(@PathVariable Long userId) {
        Long points = pointFacade.getPoint(userId);
        return ResponseEntity.ok(new PointResponse(points));
    }

    /**
     * 사용자의 포인트를 추가합니다.
     *
     * @param userId  포인트를 추가할 사용자 ID
     * @param request 추가할 포인트 금액을 포함한 요청 바디
     * @return 성공 메시지
     */
    @PostMapping("/{userId}/add")
    public ResponseEntity<PointResponse> addPoints(@PathVariable Long userId, @RequestBody PointRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("포인트 금액은 0보다 커야 합니다.");
        }

        pointFacade.rechargePoint(userId, request.getAmount());
        return ResponseEntity.ok(new PointResponse("포인트가 성공적으로 추가되었습니다."));
    }

}
