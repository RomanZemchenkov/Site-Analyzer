package searchengine.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import searchengine.services.parser.LuceneMorphologyGiver;


@Aspect
@Component
public class ServiceAspect {

    @Before(value = "searchengine.aop.pointcut.AnnotationPointcut.isLuceneInitAnnotation() " +
                    " && searchengine.aop.pointcut.ClassPointcut.isService() ",
            argNames = "joinPoint")
    public void beforeLemmasCreateMethod(JoinPoint joinPoint){
        LuceneMorphologyGiver.init();
    }
}
