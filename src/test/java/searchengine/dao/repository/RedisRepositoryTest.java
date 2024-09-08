package searchengine.dao.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class RedisRepositoryTest {

    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.4.0")
            .withExposedPorts(6379);

    private final RedisRepository redisRepository;
    private static final String KEY = "Test";

    @Autowired
    public RedisRepositoryTest(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @BeforeAll
    static void init() {
        redisContainer.start();
    }

    @Test
    @DisplayName("Тест сохранения и получения страниц из redis")
    void saveAndGetPageTest() {
        for (int i = 0; i < 100; i++) {
            redisRepository.saveUsePage(KEY, "page%d".formatted(i));
            System.out.println("Сохранение");
        }

        List<String> usePages = redisRepository.getUsePages(KEY);

        System.out.println(usePages);
        assertThat(usePages).hasSize(100);
    }

    @Test
    @DisplayName("Тест сохраниня и получения лемм из redis")
    void saveAndGetLemmaTest() {
        Site site = new Site(Status.INDEXING, OffsetDateTime.now(ZoneId.systemDefault()),"","https://sendel.ru","Sendel.ru");
        String siteName = site.getName();
        List<String> lemmas = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String lemma = "lemma" + i;
            Integer randomFrequency = new Random().nextInt(10) + 1;
            Lemma newLemma = new Lemma(lemma, randomFrequency, site);
            assertDoesNotThrow(() ->  redisRepository.saveUseLemma(siteName, newLemma));
            lemmas.add(lemma);
        }

        List<Lemma> lemmaFromRedis = redisRepository.getAllLemmasOnSite(siteName);


        assertThat(lemmaFromRedis).hasSize(100);
    }
}
