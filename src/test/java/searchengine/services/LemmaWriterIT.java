package searchengine.services;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.PageRepository;
import searchengine.dao.repository.SiteRepository;
import searchengine.services.searcher.lemma.LemmaWriter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class LemmaWriterIT extends BaseTest {

    private final IndexingService indexingService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final EntityManager entityManager;
    private static final String SITE_NAME = "Sendel.ru";
    private static final String EXIST_SITE_ID = "2";

    @Autowired
    public LemmaWriterIT(IndexingService indexingService, SiteRepository siteRepository, PageRepository pageRepository, EntityManager entityManager) {
        this.indexingService = indexingService;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.entityManager = entityManager;
    }

    @Test
    @DisplayName("Тест создания лемм для одной страницы сайта")
    void createLemmaFromOnePageTest(){
        LemmaWriter lemmaWriter = new LemmaWriter(new ConcurrentHashMap<>());
        indexingService.startIndexing();

        Site site = siteRepository.findSiteByName(SITE_NAME).get();
        Page page = pageRepository.findById(10).get();

        lemmaWriter.createLemma(page, site);

        ConcurrentHashMap<Lemma, Integer> allLemmasOnSite = lemmaWriter.getLemmasAndCounts();

        System.out.println(allLemmasOnSite.size());
        Assertions.assertThat(allLemmasOnSite).isNotEmpty();

    }
}
