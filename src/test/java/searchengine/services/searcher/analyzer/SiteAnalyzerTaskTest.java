package searchengine.services.searcher.analyzer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.BaseTest;
import searchengine.dao.model.Page;
import searchengine.dao.model.Status;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.searcher.analyzer.site_analyzer.ParseContext;
import searchengine.services.searcher.analyzer.site_analyzer.SiteAnalyzerTask;
import searchengine.services.searcher.analyzer.site_analyzer.SiteAnalyzerTaskFactory;
import searchengine.services.service.SiteService;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SiteAnalyzerTaskTest extends BaseTest {

    private final SiteAnalyzerTaskFactory factory;
    private final EntityManager entityManager;
    private final SiteService siteService;
    private static final String TEST_MAIN_URL = "https://itdeti.ru";

    @Autowired
    public SiteAnalyzerTaskTest(SiteAnalyzerTaskFactory factory, EntityManager entityManager, SiteService siteService) {
        this.factory = factory;
        this.entityManager = entityManager;
        this.siteService = siteService;
    }

    @Test
    @DisplayName("Testing the task for one site without exception")
    void successfulTaskTest() throws InterruptedException {
        ShowSiteDto siteDto = siteService.createSite(new CreateSiteDto(TEST_MAIN_URL, "ItDeti"));
        ParseContext context = new ParseContext(siteDto, factory);
        SiteAnalyzerTask task = factory.createTask(TEST_MAIN_URL, context, new ConcurrentSkipListSet<>());

        ForkJoinPool pool = new ForkJoinPool(12);

        Assertions.assertDoesNotThrow(() -> pool.invoke(task));
        pool.shutdown();

        pool.awaitTermination(100L, TimeUnit.SECONDS);

        Tuple result = entityManager.createQuery("SELECT s.status AS status, s.lastError AS lastError FROM Site AS s WHERE s.id = :id", Tuple.class)
                .setParameter("id", "4")
                .getSingleResult();

        List<Page> resultList = entityManager.createQuery("SELECT p FROM Page AS p WHERE p.site.id = 4", Page.class).getResultList();

        assertThat(result.get("lastError", String.class)).isNull();
        assertThat(resultList).hasSize(25);
    }

    @Test
    @DisplayName("Testing the task for one site with exception")
    void taskWithException() throws InterruptedException {
        ShowSiteDto site = siteService.createSite(new CreateSiteDto(TEST_MAIN_URL, "ItDeti"));
        ParseContext context = new ParseContext(site, factory);
        String badUrl = "https://itdeti.ru/112412421412";
        SiteAnalyzerTask task = factory.createTask(badUrl, context, new ConcurrentSkipListSet<>());

        ForkJoinPool pool = new ForkJoinPool(12);

        Assertions.assertDoesNotThrow(() -> pool.invoke(task));

        pool.shutdown();
        pool.awaitTermination(100L, TimeUnit.SECONDS);

        Tuple result = entityManager.createQuery("SELECT s.status AS status, s.lastError AS lastError FROM Site AS s WHERE s.id = :id", Tuple.class)
                .setParameter("id", "4")
                .getSingleResult();


        assertThat(result.get("status")).isEqualTo(Status.FAILED);
        assertThat(result.get("lastError", String.class)).isNotNull();
    }

}
