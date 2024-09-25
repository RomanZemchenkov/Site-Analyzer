package searchengine.dao.repository;


import jakarta.persistence.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.IndexingAndLemmaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class CustomStatisticRepositoryImplTest extends BaseTest {

    private final StatisticRepository statisticRepository;
    private final IndexingAndLemmaService indexingAndLemmaService;
    private final SiteRepository siteRepository;

    @Autowired
    public CustomStatisticRepositoryImplTest(StatisticRepository statisticRepository, IndexingAndLemmaService indexingAndLemmaService, SiteRepository siteRepository) {
        this.statisticRepository = statisticRepository;
        this.indexingAndLemmaService = indexingAndLemmaService;
        this.siteRepository = siteRepository;
    }

    @Test
    @DisplayName("Testing the read detail information method")
    void readDetailInformation(){
        indexingAndLemmaService.startIndexingAndCreateLemma();

        Site site = siteRepository.findSiteByName("ItDeti.ru").get();

        Tuple tuple = assertDoesNotThrow(() -> statisticRepository.readDetailInformation(site));

        assertThat(tuple.get(5, Long.class)).isEqualTo(25);
        assertThat(tuple.get(0, String.class)).isEqualTo("https://itdeti.ru");
        assertThat(tuple.get(1, String.class)).isEqualTo("ItDeti.ru");

    }
}
