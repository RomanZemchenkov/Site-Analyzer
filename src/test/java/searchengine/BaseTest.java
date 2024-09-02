package searchengine;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@Sql(value = {"classpath:sql/init.sql","classpath:sql/load.sql"})
public class BaseTest {

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16.3");
    private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.4.0").withExposedPorts(6379);

    @BeforeAll
    static void init(){
        container.start();
        redisContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",container::getJdbcUrl);
    }
}
