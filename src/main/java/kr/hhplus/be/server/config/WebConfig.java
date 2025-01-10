package kr.hhplus.be.server.config;

import kr.hhplus.be.server.api.domain.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig: 인터셉터를 등록하는 클래스.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 인증 인터셉터 등록
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") // /api/** 경로에만 적용
                .excludePathPatterns("/api/tokens/generate"); // 특정 경로 제외
    }
}
