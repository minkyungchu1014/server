package kr.hhplus.be.server;

import kr.hhplus.be.server.domain.service.QueueService;
import kr.hhplus.be.server.domain.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class AuthInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private QueueService queueService;

    @Test
    void testMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/some-endpoint"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authorization 헤더가 없습니다."));
    }

    @Test
    void testInvalidAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/some-endpoint")
                        .header("Authorization", "InvalidHeader"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authorization 헤더 형식이 유효하지 않습니다."));
    }

    @Test
    void testValidToken() throws Exception {
        when(tokenService.tokenExists("valid-token")).thenReturn(true);
        when(tokenService.isTokenExpired("valid-token")).thenReturn(false);
        when(queueService.isQueueActive("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/some-endpoint")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }
}
