package searchengine.aop.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.exception.EmptyQueryException;
import searchengine.services.exception.IndexingStartingException;
import searchengine.services.exception.SiteDoesntExistException;
import searchengine.services.parser.LuceneMorphologyGiver;

import java.util.Optional;

import static searchengine.services.GlobalVariables.*;


@Aspect
@Component
@RequiredArgsConstructor
public class ServiceAspect {

    private final SiteRepository siteRepository;

    @Before(value = "searchengine.aop.pointcut.AnnotationPointcut.isLuceneInitAnnotation() " +
                    " && searchengine.aop.pointcut.ClassPointcut.isService() ",
            argNames = "joinPoint")
    public void beforeLemmasCreateMethod(JoinPoint joinPoint){
        LuceneMorphologyGiver.init();
    }

    @Before(value = "searchengine.aop.pointcut.ClassPointcut.isService() " +
                    "&& searchengine.aop.pointcut.AnnotationPointcut.isCheckQueryAnnotation() " +
                    "&& args(dto)", argNames = "joinPoint,dto")
    public void beforeSearchMethod(JoinPoint joinPoint, SearchParametersDto dto){
        String query = dto.getQuery();
        if(query.isBlank()){
            throw new EmptyQueryException();
        }
    }

    @Before(value = "searchengine.aop.pointcut.ClassPointcut.isService() " +
                    "&& searchengine.aop.pointcut.AnnotationPointcut.isCheckIndexingWorkAnnotation()")
    public void indexingWorkChecking(JoinPoint joinPoint){
        if (INDEXING_STARTED || LEMMA_CREATING_STARTED || INDEX_CREATING_STARTED) {
            throw new IndexingStartingException();
        }
    }

    @Before(value = "searchengine.aop.pointcut.ClassPointcut.isService() " +
                    "&& searchengine.aop.pointcut.AnnotationPointcut.isCheckSiteExistAnnotation() " +
                    "&& args(dto)", argNames = "joinPoint,dto")
    public void siteExistChecking(JoinPoint joinPoint, SearchParametersDto dto){
        String url = dto.getUrl();
        if(url != null && !url.isBlank()){
            Optional<Site> mayBeSite = siteRepository.findSiteByUrl(url);
            if(mayBeSite.isEmpty()){
                throw new SiteDoesntExistException();
            }
        }
    }
}
