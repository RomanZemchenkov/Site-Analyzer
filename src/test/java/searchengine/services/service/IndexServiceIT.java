package searchengine.services.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.searcher.analyzer.IndexingImpl;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexServiceIT extends BaseTest {

    private final LemmaService lemmaService;
    private final SiteRepository siteRepository;
    private final IndexingImpl indexing;
    private final IndexService indexService;
    private final EntityManager entityManager;
    private static final String EXIST_SITE_NAME = "Sendel.ru";

    @Autowired
    public IndexServiceIT(LemmaService lemmaService, SiteRepository siteRepository, IndexingImpl indexing, IndexService indexService, EntityManager entityManager) {
        this.lemmaService = lemmaService;
        this.siteRepository = siteRepository;
        this.indexing = indexing;
        this.indexService = indexService;
        this.entityManager = entityManager;
    }

//    @Test
//    @DisplayName("Testing the create index method")
//    void createIndex(){
//        indexing.startSitesIndexing();
//        Optional<Site> mayBeSite = siteRepository.findSiteByName(EXIST_SITE_NAME);
//        Assertions.assertTrue(mayBeSite.isPresent());
//
//        Site site = mayBeSite.get();
//        ExecutorService threadPool = Executors.newCachedThreadPool();
//
//        LemmaCreatorTaskFactory factory = new LemmaCreatorTaskFactory();
//        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
//                new ConcurrentLinkedDeque<>(site.getPages()), factory, new ConcurrentHashMap<>());
//        LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
//
//        List<Lemma> lemmaList = new ArrayList<>();
//        threadPool.submit(() -> {
//            ForkJoinPool forkJoinPool = new ForkJoinPool();
//            lemmaList.addAll(forkJoinPool.invoke(task));
//            forkJoinPool.shutdown();
//        });
//
//        threadPool.shutdown();
//
//        try {
//            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        lemmaService.createBatch(lemmaList);
//
//        Assertions.assertDoesNotThrow(indexService::createIndex);
//
//        Long result = entityManager.createQuery("SELECT count(i) FROM Index i", Long.class).getSingleResult();
//
//        assertThat(result).isEqualTo(6281);
//    }
}
