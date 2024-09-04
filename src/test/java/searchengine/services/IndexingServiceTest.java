package searchengine.services;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class IndexingServiceTest extends BaseTest {

    private final IndexingService service;
    private final EntityManager manager;

    @Autowired
    public IndexingServiceTest(IndexingService service, EntityManager manager) {
        this.service = service;
        this.manager = manager;
    }

    @Test
    @DisplayName("Тестирование запуска индексирования без заложенных проблем")
    void startIndexingWithoutProblemTest() {
        Assertions.assertDoesNotThrow(service::startIndexing);
    }

    @Test
    @DisplayName("Тестирование запуска с принудительной остановкой")
    void startIndexingWithStopIndexingTest() {
        Assertions.assertDoesNotThrow(() -> {
            Thread threadStart = new Thread(service::startIndexing);
            Thread threadStop = new Thread(service::stopIndexing);

            threadStart.start();
            Thread.sleep(500);

            threadStop.start();

            threadStart.join();
            threadStop.join();
        });

        List<String> lastError = manager.createQuery("SELECT s.lastError FROM Site AS s WHERE s.id >= 3", String.class)
                .getResultList();

        lastError.forEach(er -> assertThat(er).isNotBlank());
    }
}
