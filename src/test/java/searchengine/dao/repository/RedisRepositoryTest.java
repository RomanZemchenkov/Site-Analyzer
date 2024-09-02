package searchengine.dao.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

@SpringBootTest
public class RedisRepositoryTest{

    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.4.0")
            .withExposedPorts(6379);

    private final RedisRepository redisRepository;
    private static final String KEY = "Test";

    @Autowired
    public RedisRepositoryTest(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @BeforeAll
    static void init(){
        redisContainer.start();
    }

    @Test
    @DisplayName("Тест сохранения и получения объектов из redis")
    void testSaveAndGet(){
        for(int i = 0; i < 100; i++){
            redisRepository.saveUsePage(KEY,"page%d".formatted(i));
            System.out.println("Сохранение");
        }

        List<String> usePages = redisRepository.getUsePages(KEY);

        System.out.println(usePages);
        Assertions.assertThat(usePages).hasSize(100);
    }
}
