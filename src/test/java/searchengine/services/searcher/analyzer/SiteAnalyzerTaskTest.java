package searchengine.services.searcher.analyzer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.BaseTest;
import searchengine.dao.model.Page;
import searchengine.dao.model.Status;
import searchengine.dao.repository.RedisRepository;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SiteAnalyzerTaskTest extends BaseTest {

    private final SiteAnalyzerTaskFactory factory;
    private final EntityManager entityManager;

    @Autowired
    public SiteAnalyzerTaskTest(SiteAnalyzerTaskFactory factory, EntityManager entityManager) {
        this.factory = factory;
        this.entityManager = entityManager;
    }

    @Test
    @DisplayName("Testing the task for one site without exception")
    void successfulTaskTest() throws InterruptedException {
        String url = "https://sendel.ru";
        ParseContext context = new ParseContext("2", "Sendel.ru", url, factory);
        SiteAnalyzerTask task = factory.createTask(url, context, new ConcurrentSkipListSet<>());

        ForkJoinPool pool = new ForkJoinPool(12);

        Assertions.assertDoesNotThrow(() -> pool.invoke(task));
        pool.shutdown();

        pool.awaitTermination(100L, TimeUnit.SECONDS);

        Tuple result = entityManager.createQuery("SELECT s.status AS status, s.lastError AS lastError FROM Site AS s WHERE s.id = :id", Tuple.class)
                .setParameter("id", "1")
                .getSingleResult();

        List<Page> resultList = entityManager.createQuery("SELECT p FROM Page AS p WHERE p.site.name = 'Sendel.ru'", Page.class).getResultList();

        assertThat(result.get("status")).isEqualTo(Status.INDEXED);
        assertThat(result.get("lastError", String.class)).isNull();
        assertThat(resultList).hasSize(78);
    }

    @Test
    @DisplayName("Testing the task for one site with exception")
    void taskWithException() throws InterruptedException {
        String url = "https://sendel.ru/2242421";

        ParseContext context = new ParseContext("2", "Sendel.ru", url, factory);
        SiteAnalyzerTask task = factory.createTask(url, context, new ConcurrentSkipListSet<>());

        ForkJoinPool pool = new ForkJoinPool(12);

        Assertions.assertDoesNotThrow(() -> pool.invoke(task));

        pool.shutdown();
        pool.awaitTermination(100L, TimeUnit.SECONDS);

        Tuple result = entityManager.createQuery("SELECT s.status AS status, s.lastError AS lastError FROM Site AS s WHERE s.id = :id", Tuple.class)
                .setParameter("id", "2")
                .getSingleResult();


        assertThat(result.get("status")).isEqualTo(Status.FAILED);
        assertThat(result.get("lastError", String.class)).isNotNull();
    }

}
