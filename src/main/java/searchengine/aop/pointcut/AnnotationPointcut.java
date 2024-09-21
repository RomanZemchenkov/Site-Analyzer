package searchengine.aop.pointcut;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AnnotationPointcut {

    @Pointcut(value = "@annotation(searchengine.aop.annotation.LuceneInit)")
    public void isLuceneInitAnnotation(){}


    @Pointcut(value = "@args(org.springframework.stereotype.Service)")
    public void isServiceAnnotation(){}
}
