package searchengine.aop.pointcut;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class ClassPointcut {

    @Pointcut(value = "within(searchengine.services..*)")
    public void isService(){}

}
