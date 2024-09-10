package searchengine.dao.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
