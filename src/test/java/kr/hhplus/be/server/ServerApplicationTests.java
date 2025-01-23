package kr.hhplus.be.server;

import kr.hhplus.be.server.api.domain.controller.PaymentController;
import kr.hhplus.be.server.domain.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ServerApplicationTests {

	@Autowired
	private PaymentController paymentController;

	@Autowired
	private PaymentService paymentService;

	@Test
	void contextLoads() {
		// 애플리케이션 컨텍스트가 정상적으로 로드되고, 빈이 주입되는지 확인
		assertNotNull(paymentController, "PaymentController should not be null");
		assertNotNull(paymentService, "PaymentService should not be null");
	}
}
