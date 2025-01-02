package kr.hhplus.be.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ServerConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();  // 외부 API 호출을 위한 RestTemplate
    }
}
