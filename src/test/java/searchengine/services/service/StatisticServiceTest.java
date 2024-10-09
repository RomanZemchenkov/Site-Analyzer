package searchengine.services.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.IndexingService;
import searchengine.services.dto.statistics.DetailedStatisticsItem;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.dto.statistics.TotalStatistics;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class StatisticServiceTest extends BaseTest {

    private final StatisticsService statisticsService;
    private final IndexingService indexingAndLemmaService;

    @Autowired
    public StatisticServiceTest(StatisticsService statisticsService, IndexingService indexingAndLemmaService) {
        this.statisticsService = statisticsService;
        this.indexingAndLemmaService = indexingAndLemmaService;
    }

    @Test
    @DisplayName("Testing the read statistic method after indexing method")
    void readTotalStatistic(){
        indexingAndLemmaService.startIndexingAndCreateLemma();
        TotalStatistics totalStatistics = assertDoesNotThrow(statisticsService::readTotalStatistics);

        assertThat(totalStatistics.getSites()).isEqualTo(4);
        assertThat(totalStatistics.isIndexing()).isEqualTo(false);
        assertThat(totalStatistics.getPages()).isEqualTo(29);
    }

    @Test
    @DisplayName("Testing the read statistic method while indexing is running")
    void readTotalStatisticWhileIndexingIsRunning() throws InterruptedException {
        Thread indexingThread = new Thread(indexingAndLemmaService::startIndexingAndCreateLemma);
        Thread statisticThread = new Thread(() -> {
            TotalStatistics totalStatistics = assertDoesNotThrow(statisticsService::readTotalStatistics);
            System.out.println(totalStatistics);
            assertThat(totalStatistics.getPages()).isEqualTo(29);
            assertThat(totalStatistics.getSites()).isEqualTo(4);
            assertThat(totalStatistics.getLemmas()).isNotNull();
        });

        indexingThread.start();

        Thread.sleep(3000L);

        statisticThread.start();

        indexingThread.join();
        statisticThread.join();
    }

    @Test
    @DisplayName("Testing the read detail statistic method")
    void readDetailStatistic(){
        indexingAndLemmaService.startIndexingAndCreateLemma();

        List<DetailedStatisticsItem> statisticsItems = assertDoesNotThrow(statisticsService::readDetailStatistic);

        assertThat(statisticsItems).hasSize(4);
    }

    @Test
    @DisplayName("Testing the get total statistic method")
    void getTotalStatistic(){
        indexingAndLemmaService.startIndexingAndCreateLemma();

        StatisticsResponse statisticsResponse = assertDoesNotThrow(statisticsService::getTotalStatistic);

        assertThat(statisticsResponse.isResult()).isTrue();
    }
}
