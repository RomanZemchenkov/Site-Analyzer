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

    @Pointcut(value = "@annotation(searchengine.aop.annotation.CheckQuery)")
    public void isCheckQueryAnnotation(){}

    @Pointcut(value = "@annotation(searchengine.aop.annotation.CheckIndexingWork)")
    public void isCheckIndexingWorkAnnotation(){}

    @Pointcut(value = "@annotation(searchengine.aop.annotation.CheckSiteExist)")
    public void isCheckSiteExistAnnotation(){}
}
