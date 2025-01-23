package kr.hhplus.be.server.api.domain.controller;

import kr.hhplus.be.server.api.domain.usecase.TokenFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenFacade tokenFacade;

    /**
     * TokenController 생성자
     * @param tokenFacade Token 관련 비즈니스 로직을 처리하는 Facade
     */
    public TokenController(TokenFacade tokenFacade) {
        this.tokenFacade = tokenFacade;
    }

    /**
     * 새로운 토큰을 생성합니다.
     * @param userId 사용자 ID
     * @return 생성된 토큰 값
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateToken(@RequestParam Long userId) {
        try {
            String token = tokenFacade.generateToken(userId);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("토큰 생성 과정에 오류가 있습니다." + e.getMessage());
        }
    }
}
