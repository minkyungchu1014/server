package kr.hhplus.be.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.api.domain.interceptor.AuthInterceptor;
import kr.hhplus.be.server.domain.service.QueueService;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class AuthInterceptorTest {

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @Mock
    private TokenService tokenService;

    @Mock
    private QueueService queueService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMissingAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더가 없습니다.");
    }

    @Test
    void testInvalidAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization 헤더 형식이 유효하지 않습니다.");
    }

    @Test
    void testValidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenService.tokenExists("valid-token")).thenReturn(true);
        when(tokenService.isTokenExpired("valid-token")).thenReturn(false);
        when(queueService.isQueueActive("valid-token")).thenReturn(true);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void testTokenExistsButNotActive() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenService.tokenExists("valid-token")).thenReturn(true);
        when(tokenService.isTokenExpired("valid-token")).thenReturn(false);
        when(queueService.isQueueActive("valid-token")).thenReturn(false);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response, times(1)).sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
    }
}
