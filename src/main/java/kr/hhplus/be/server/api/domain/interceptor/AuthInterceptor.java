package kr.hhplus.be.server.api.domain.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.domain.service.QueueService;
import kr.hhplus.be.server.domain.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * AuthInterceptor: 요청 전 토큰 인증을 수행하는 인터셉터.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);
    private final TokenService tokenService;
    private final QueueService queueService;

    @Value("${spring.profiles.active:default}")
    private String activeProfile; // 현재 활성화된 프로파일

    public AuthInterceptor(TokenService tokenService, QueueService queueService) {
        this.tokenService = tokenService;
        this.queueService = queueService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 프로파일 확인 (테스트 환경에서는 인증 생략)
        if ("test".equals(activeProfile)) {
            logger.info("테스트 환경: 인증 생략");
            return true;
        }

        String authorizationHeader = request.getHeader("Authorization");

        // 헤더 출력 (디버깅용)
        logger.info("Received Headers: ");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            logger.info("{}: {}", headerName, request.getHeader(headerName));
        });

        // Authorization 헤더 확인
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            logger.info("토큰 유실: Authorization 헤더가 존재하지 않습니다.");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더가 없습니다.");
            return false;
        }

        // Bearer 토큰 파싱
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            logger.info("유효하지 않은 Authorization 헤더 형식");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더 형식이 유효하지 않습니다.");
            return false;
        }

        // 토큰 유효성 확인
        if (!tokenService.tokenExists(token)) {
            logger.info("유효하지 않은 토큰: {}", token);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return false;
        }

        // 토큰 만료 여부 확인
        if (tokenService.isTokenExpired(token)) {
            logger.info("토큰 만료: {}", token);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
            return false;
        }

        // 대기열 상태 확인
        if (!queueService.isQueueActive(token)) {
            logger.info("대기열 검증 실패: {}", token);
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "대기열 검증에 실패했습니다.");
            return false;
        }

        // 모든 검증 통과
        return true;
    }

    /**
     * Bearer 토큰 파싱
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return 파싱된 토큰 값 또는 null (잘못된 형식일 경우)
     */
    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 이후의 토큰 값 반환
        }
        return null;
    }

    /**
     * 에러 응답 전송
     *
     * @param response HttpServletResponse 객체
     * @param status   HTTP 상태 코드
     * @param message  에러 메시지
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }
}
