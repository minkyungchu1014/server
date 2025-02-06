package kr.hhplus.be.server;

import kr.hhplus.be.server.domain.service.RedisTestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication(scanBasePackages = "kr.hhplus.be")
@EnableJpaRepositories
public class ServerApplication {

	public static void main(String[] args) {
		// 1. Spring Boot 애플리케이션 실행
		ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);

		// 2. Bean 목록 출력 (디버깅용)
		String[] beanNames = context.getBeanDefinitionNames();
		Arrays.stream(beanNames).forEach(System.out::println);

		// 3. RedisService 실행 (Redis 연결 테스트)
		RedisTestService redisService = context.getBean(RedisTestService.class);
		redisService.testRedis();
	}
}
