package searchengine.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.BaseTest;
import searchengine.dao.model.Status;
import searchengine.services.searcher.indexing.ParseContext;
import searchengine.services.searcher.indexing.SiteAnalyzerTask;
import searchengine.services.searcher.indexing.SiteAnalyzerTaskFactory;

import java.util.concurrent.ForkJoinPool;

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
    @DisplayName("Тест удачного обхода сайта")
    void successfulTaskTest() {
        String url = "https://sendel.ru";
        ParseContext context = new ParseContext("1", "site", url, factory);
        SiteAnalyzerTask task = factory.createTask(url, context);

        ForkJoinPool pool = new ForkJoinPool(12);

        Assertions.assertDoesNotThrow(() -> pool.invoke(task));

        Tuple result = entityManager.createQuery("SELECT s.status AS status, s.lastError AS lastError FROM Site AS s WHERE s.id = :id", Tuple.class)
                .setParameter("id", "1")
                .getSingleResult();

        assertThat(result.get("status")).isEqualTo(Status.INDEXING);
        assertThat(result.get("lastError", String.class)).isNull();
    }

    @Test
    @DisplayName("Тест обхода сайта с ошибкой")
    void taskWithException(){
        String url = "https://sendel.ru/2242421";

        ParseContext context = new ParseContext("1", "site", url, factory);
        SiteAnalyzerTask task = factory.createTask(url, context);

        ForkJoinPool pool = new ForkJoinPool(12);

        Assertions.assertDoesNotThrow(() -> pool.invoke(task));

        Tuple result = entityManager.createQuery("SELECT s.status AS status, s.lastError AS lastError FROM Site AS s WHERE s.id = :id", Tuple.class)
                .setParameter("id", "1")
                .getSingleResult();

        assertThat(result.get("status")).isEqualTo(Status.FAILED);
        assertThat(result.get("lastError", String.class)).isNotNull();
    }
}
