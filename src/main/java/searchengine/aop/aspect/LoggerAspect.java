package searchengine.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("logging")
public class LoggerAspect {

    private static final Logger CONSOLE_LOGGER = LoggerFactory.getLogger("TIME_LOGGER");

    @Around(value = "searchengine.aop.pointcut.AnnotationPointcut.isCheckTimeWorkingAnnotation()")
    public Object checkTimeWorking(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed;
        try {
            proceed = joinPoint.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            Signature signature = joinPoint.getSignature();
            String methodName = signature.getName();
            String className = signature.getDeclaringTypeName();
            long duration = finish - start;
            CONSOLE_LOGGER.info("Method {} in class {} executed in {} ms", methodName, className, duration);
        }
        return proceed;
    }
}
