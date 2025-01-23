package kr.hhplus.be.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // SecurityScheme 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization") // 헤더 이름
                .type(SecurityScheme.Type.APIKEY) // API Key 타입으로 설정
                .in(SecurityScheme.In.HEADER); // 헤더에서 토큰 읽기

        // OpenAPI 설정
        return new OpenAPI()
                .info(new Info()
                        .title("API Documentation")
                        .description("API 설명 문서")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Authorization", securityScheme));
    }
}
