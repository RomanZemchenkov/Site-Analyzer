package searchengine.services.searcher.lemma;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.page.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.searcher.analyzer.Indexing;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class LemmaWriterIT extends BaseTest {

    private final Indexing indexingService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private static final String SITE_NAME = "Sendel.ru";

    @Autowired
    public LemmaWriterIT(Indexing indexingService, SiteRepository siteRepository, PageRepository pageRepository) {
        this.indexingService = indexingService;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Test
    @DisplayName("Testing the lemma creating for one page")
    void createLemmaFromOnePageTest(){
        LemmaWriter lemmaWriter = new LemmaWriter(new ConcurrentHashMap<>());
        indexingService.startIndexing();

        Site site = siteRepository.findSiteByName(SITE_NAME).get();
        Page page = pageRepository.findById(10).get();

        assertDoesNotThrow(() -> lemmaWriter.createLemma(page, site));

        ConcurrentHashMap<Lemma, Integer> allLemmasOnSite = lemmaWriter.getLemmasAndCounts();

        assertThat(allLemmasOnSite).isNotEmpty();

    }
}
