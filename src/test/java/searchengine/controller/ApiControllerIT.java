package searchengine.controller;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import searchengine.BaseTest;
import searchengine.services.exception.ExceptionMessage;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
public class ApiControllerIT extends BaseTest {

    private final MockMvc mock;

    @Autowired
    public ApiControllerIT(MockMvc mock) {
        this.mock = mock;
    }


    @Test
    @DisplayName("Тестирование полной индексации сайтов")
    void fullIndexingSiteTest() throws Exception {
        ResultActions actions = mock.perform(get("/api/startIndexing"));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result", Matchers.is("true")));

    }

    @Test
    @DisplayName("Тестирование индексации с ошибкой")
    void indexingWithStatusError() throws Exception {
        ResultActions actions = mock.perform(get("/api/startIndexing"));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result", Matchers.is("true")));

    }

    @Test
    @DisplayName("Тестирование начала индексации при работающем сервисе")
    void indexingWithRunningServiceTest() throws Exception {
        AtomicBoolean checkTryAction = new AtomicBoolean(false);
        Thread threadStartIndexing = new Thread(() -> {
            try {
                mock.perform(get("/api/startIndexing"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread threadOneMoreStartIndexing = new Thread(() -> {
            try {
                ResultActions tryAction = mock.perform(get("/api/startIndexing"));
                tryAction.andExpect(status().isConflict())
                        .andExpect(jsonPath("$.result", Matchers.is("false")))
                        .andExpect(jsonPath("$.message", Matchers.is("Индексация уже запущена.")));
                checkTryAction.set(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        threadStartIndexing.start();

        Thread.sleep(1000L);

        threadOneMoreStartIndexing.start();

        threadStartIndexing.join();
        threadOneMoreStartIndexing.join();


        Assertions.assertThat(checkTryAction.get()).isTrue();
    }

    @Test
    @DisplayName("Тестирование индексации с остановкой")
    void indexingWithStopTest() throws Exception {
        AtomicBoolean checkTryAction = new AtomicBoolean(false);
        Thread threadStartIndexing = new Thread(() -> {
            try {
                mock.perform(get("/api/startIndexing"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread threadStopIndexing = new Thread(() -> {
            try {
                ResultActions tryAction = mock.perform(get("/api/stopIndexing"));
                tryAction.andExpect(status().isOk())
                        .andExpect(jsonPath("$.result", Matchers.is("true")));
                checkTryAction.set(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        threadStartIndexing.start();
        Thread.sleep(1000L);
        threadStopIndexing.start();


        threadStartIndexing.join();
        threadStopIndexing.join();


        Assertions.assertThat(checkTryAction.get()).isTrue();
    }

    @Test
    @DisplayName("Тестирование неработающей индексации с остановкой")
    void noIndexingWithStopTest() throws Exception {
        ResultActions tryAction = mock.perform(get("/api/stopIndexing"));
        tryAction.andExpect(status().isConflict())
                .andExpect(jsonPath("$.result", Matchers.is("false")))
                .andExpect(jsonPath("$.message", Matchers.is("Индексация не запущена.")));

    }

    @Test
    @DisplayName("Удачное тестирование индексации одной страницы")
    void successfulOnePageIndexingTest() throws Exception {
        ResultActions actions = mock.perform(post("/api/indexPage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "url" : "https://sendel.ru/posts/java-with-vscode/"
                        }
                        """));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result",Matchers.is("true")));
    }

    @Test
    @DisplayName("Недачное тестирование индексации одной страницы")
    void unsuccessfulOnePageIndexingTest() throws Exception {
        ResultActions actions = mock.perform(post("/api/indexPage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "url" : "https://ru.wikipedia.org/wiki/Заглавная_страница"
                        }
                        """));

        actions.andExpect(status().isConflict())
                .andExpect(jsonPath("$.result",Matchers.is("false")))
                .andExpect(jsonPath("$.message", Matchers.is("Данная страница находится за пределами сайтов, указанных в конфигурационном файле.")));
    }
}
