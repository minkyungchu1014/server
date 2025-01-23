package kr.hhplus.be.server.api.domain.interceptor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Pointcut: 특정 패키지의 모든 메서드를 대상으로 지정
    @Pointcut("execution(* kr.hhplus.be.server..*(..))") // 대상 패키지 경로로 변경
    public void serviceMethods() {}


    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logMethodReturnValue(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info("Class: {}, Method: {}, Args: {}, Returned: {}", className, methodName, args, result);
    }
}
