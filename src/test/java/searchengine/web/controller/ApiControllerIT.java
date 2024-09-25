package searchengine.web.controller;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import searchengine.BaseTest;
import searchengine.services.exception.ExceptionMessage;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static searchengine.services.exception.ExceptionMessage.*;


@AutoConfigureMockMvc
@SpringBootTest
@Sql(value = "classpath:sql/init.sql")
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
                        .andExpect(jsonPath("$.message", Matchers.is(INDEXING_ALREADY_START)));
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
                .andExpect(jsonPath("$.message", Matchers.is(INDEXING_DOESNT_START)));

    }

    @Test
    @DisplayName("Удачное тестирование индексации одной страницы")
    void successfulOnePageIndexingTest() throws Exception {
        ResultActions actions = mock.perform(post("/api/indexPage")
                        .param("url","https://itdeti.ru/robotrack"));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result",Matchers.is("true")));
    }

    @Test
    @DisplayName("Недачное тестирование индексации одной страницы")
    void unsuccessfulOnePageIndexingTest() throws Exception {
        ResultActions actions = mock.perform(post("/api/indexPage")
                .param("url","https://ru.wikipedia.org/wiki/Заглавная_страница"));

        actions.andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.result",Matchers.is("false")))
                .andExpect(jsonPath("$.message", Matchers.is(ILLEGAL_PAGE_EXCEPTION)));
    }

    @Test
    @DisplayName("Testing the successful statistics create")
    void successfulStatisticsCreate() throws Exception {
        mock.perform(get("/api/startIndexing"));

        ResultActions actions = mock.perform(get("/api/statistics"));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result",Matchers.is(true)))
                .andExpect(jsonPath("$.statistics",Matchers.notNullValue()))
                .andExpect(jsonPath("$.statistics.total",Matchers.notNullValue()))
                .andExpect(jsonPath("$.statistics.total.sites",Matchers.is(1)))
                .andExpect(jsonPath("$.statistics.total.pages",Matchers.is(25)))
                .andExpect(jsonPath("$.statistics.total.lemmas",Matchers.is(2732)));
    }

    @ParameterizedTest
    @DisplayName("Testing the statistics response while indexing works")
    @MethodSource("argumentsForStatisticsResponseWhileIndexingWorks")
    void statisticsResponseWhileIndexingWorks(boolean expectedIndexing, long timeSleep) throws Exception {
        Thread indexingThread = new Thread(() -> {
            try {
                mock.perform(get("/api/startIndexing"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread statisticsThread = new Thread(() -> {
            try {
                ResultActions actions = mock.perform(get("/api/statistics"));
                actions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.result",Matchers.is(true)))
                        .andExpect(jsonPath("$.statistics",Matchers.notNullValue()))
                        .andExpect(jsonPath("$.statistics.total",Matchers.notNullValue()))
                        .andExpect(jsonPath("$.statistics.total.indexing",Matchers.is(expectedIndexing)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        indexingThread.start();
        Thread.sleep(timeSleep);
        statisticsThread.start();

        indexingThread.join();
        statisticsThread.join();

    }

    static Stream<Arguments> argumentsForStatisticsResponseWhileIndexingWorks(){
        return Stream.of(
                Arguments.of(true,1000L),
                Arguments.of(false,3000L)
        );
    }

    @ParameterizedTest
    @DisplayName("Testing the search with few results by one site")
    @MethodSource("argumentsForSearchWithFewResultsByOneSiteTest")
    void successfulSearchWithFewResultsByOneSite(String query, String useUrl, String limit,String offset,int expectedResult, int expectedDataSize) throws Exception {
        mock.perform(get("/api/startIndexing"));

        ResultActions actions = mock.perform(get("/api/search")
                .param("query", query)
                .param("url", useUrl)
                .param("limit", limit)
                .param("offset", offset));

        actions.andExpect(jsonPath("$.result",Matchers.is(true)))
                .andExpect(jsonPath("$.count",Matchers.is(expectedResult)))
                .andExpect(jsonPath("$.data",Matchers.hasSize(expectedDataSize)));
    }

    static Stream<Arguments> argumentsForSearchWithFewResultsByOneSiteTest(){
        return Stream.of(
                Arguments.of("Написание скриптов","https://itdeti.ru","2","0",3,2),
                Arguments.of("Написание скриптов","https://itdeti.ru","3","0",3,3),
                Arguments.of("Написание скриптов","https://itdeti.ru","3","1",3,3),
                Arguments.of("Написание скриптов","https://itdeti.ru","2","1",3,1)
        );
    }

    @Test
    @DisplayName("Testing the search with few results by several site")
    void successfulSearchWithFewResultsBySeveralSite() throws Exception {
        mock.perform(get("/api/startIndexing"));

        ResultActions actions = mock.perform(get("/api/search")
                .param("query", "Написание скриптов")
                .param("url", "")
                .param("limit", "4")
                .param("offset", "0"));

        actions.andExpect(jsonPath("$.result",Matchers.is(true)))
                .andExpect(jsonPath("$.count",Matchers.is(4)))
                .andExpect(jsonPath("$.data",Matchers.hasSize(4)));
    }


    @Test
    @DisplayName("Testing the search with indexingException")
    void searchWithException() throws Exception {

        Thread indexingThread = new Thread(() -> {
            try {
                mock.perform(get("/api/startIndexing"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread searchThread = new Thread(() -> {
            try {
                ResultActions actions = mock.perform(get("/api/search")
                        .param("query", "Написание скриптов")
                        .param("url", "https://itdeti.ru")
                        .param("limit", "2")
                        .param("offset", "2"));

                actions.andExpect(jsonPath("$.result", Matchers.is("false")))
                        .andExpect(jsonPath("$.message",Matchers.is(ExceptionMessage.INDEXING_STARTING_EXCEPTION)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        indexingThread.start();;

        Thread.sleep(1000L);

        searchThread.start();;

        indexingThread.join();
        searchThread.join();

        ResultActions actions = mock.perform(get("/api/search")
                .param("query", "Написание скриптов")
                .param("url", "https://itdeti.ru")
                .param("limit", "2")
                .param("offset", "2"));

        actions.andExpect(jsonPath("$.result",Matchers.is(true)))
                .andExpect(jsonPath("$.count",Matchers.is(3)))
                .andExpect(jsonPath("$.data",Matchers.hasSize(1)));

    }

    @Test
    @DisplayName("Testing the search with empty query")
    void searchWithEmptyQuery() throws Exception {
        mock.perform(get("/api/startIndexing"));


        ResultActions actions = mock.perform(get("/api/search")
                .param("query", "")
                .param("url", "https://itdeti.ru")
                .param("limit", "2")
                .param("offset", "2"));

        actions.andExpect(jsonPath("$.result", Matchers.is("false")))
                .andExpect(jsonPath("$.message",Matchers.is(ExceptionMessage.EMPTY_QUERY_EXCEPTION)));
    }

    @Test
    @DisplayName("Testing the search with site doesn`t exist")
    void searchWithSiteDoesntExist() throws Exception {
        mock.perform(get("/api/startIndexing"));


        ResultActions actions = mock.perform(get("/api/search")
                .param("query", "Написание скриптов")
                .param("url", "https://randomSite.tut")
                .param("limit", "2")
                .param("offset", "2"));

        actions.andExpect(jsonPath("$.result", Matchers.is("false")))
                .andExpect(jsonPath("$.message",Matchers.is(ExceptionMessage.SITE_DOESNT_EXIST_EXCEPTION)));
    }


}
