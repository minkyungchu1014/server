package kr.hhplus.be.server.api.domain.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.domain.service.QueueService;
import kr.hhplus.be.server.domain.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * AuthInterceptor: 요청 전 토큰 인증을 수행하는 인터셉터.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final TokenService tokenService;
    private final QueueService queueService;

    public AuthInterceptor(TokenService tokenService, QueueService queueService) {
        this.tokenService = tokenService;
        this.queueService = queueService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        logger.info("Received Headers: ");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            logger.info("{}: {}", headerName, request.getHeader(headerName));
        });

        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("토큰 유실");
            logger.info("토큰 유실 : ", token);
            return false;
        }


        // 토큰 유효성 확인
        if (!tokenService.tokenExists(token) || !tokenService.isTokenExpired(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("유효하지 않은 토큰");
            logger.info("유효하지 않은 토큰");
            return false;
        }

        // 대기열 상태 확인
        if (!queueService.isQueueActive(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("대기열 검증 실패");
            logger.info("대기열 검증 실패");
            return false;
        }

        return true; // 검증 통과
    }

}
