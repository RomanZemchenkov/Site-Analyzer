package searchengine.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.dao.repository.RedisRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class IndexingServiceTest extends BaseTest {

    private final IndexingService service;
    private final EntityManager manager;
    private final RedisRepository redisRepository;

    @Autowired
    public IndexingServiceTest(IndexingService service, EntityManager manager, RedisRepository redisRepository) {
        this.service = service;
        this.manager = manager;
        this.redisRepository = redisRepository;
    }

    @Test
    @DisplayName("Тестирование запуска индексирования без заложенных проблем")
    void startIndexingWithoutProblemTest() {
        Assertions.assertDoesNotThrow(service::startIndexing);

        List<Site> sites = manager.createQuery("SELECT s FROM Site AS s WHERE s.id >= 3", Site.class)
                .getResultList();

        System.out.println(sites);

        sites.forEach(er -> {
            assertThat(er.getLastError()).isNull();
            assertThat(er.getPages()).isNotNull();
            assertThat(er.getStatus()).isEqualTo(Status.INDEXED);
        });
    }

    @Test
    @DisplayName("Тестирование запуска индексирования с проблемным сайтом")
    void startIndexingWithProblemTest() {
        Assertions.assertDoesNotThrow(service::startIndexing);

        List<Site> sites = manager.createQuery("SELECT s FROM Site AS s WHERE s.id >= 3", Site.class)
                .getResultList();

        System.out.println(sites);

        sites.forEach(er -> {
            assertThat(er.getLastError()).isNotBlank();
            assertThat(er.getPages()).isNotNull();
            assertThat(er.getStatus()).isEqualTo(Status.FAILED);
        });
    }

    @Test
    @DisplayName("Тестирование запуска нормальных сайтов и сайта с проблемой")
    void startIndexingNormalAndProblemSitesTest() {
        Assertions.assertDoesNotThrow(service::startIndexing);

        List<Site> sites = manager.createQuery("SELECT s FROM Site AS s WHERE s.id >= 3", Site.class)
                .getResultList();

        System.out.println(sites);
    }

    @Test
    @DisplayName("Тестирование запуска с принудительной остановкой")
    void startIndexingWithStopIndexingTest() {
        Assertions.assertDoesNotThrow(() -> {
            Thread threadStart = new Thread(service::startIndexing);
            Thread threadStop = new Thread(service::stopIndexing);

            threadStart.start();
            Thread.sleep(1000L);

            threadStop.start();

            threadStart.join();
            threadStop.join();
        });

        List<Site> sites = manager.createQuery("SELECT s FROM Site AS s WHERE s.id >= 3", Site.class)
                .getResultList();

        sites.forEach(er -> {
            assertThat(er.getLastError()).isNotBlank();
            assertThat(er.getPages()).isNotNull();
            assertThat(er.getStatus()).isEqualTo(Status.FAILED);
        });

    }

    @AfterEach
    void down(){
        redisRepository.clearListByUrl("*");
    }

}
