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
@ActiveProfiles({"test","logging"})
@Sql(value = {"classpath:sql/init.sql","classpath:sql/load.sql"})
public class BaseTest {

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16.3");

    @BeforeAll
    static void init(){
        container.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",container::getJdbcUrl);
    }
}
