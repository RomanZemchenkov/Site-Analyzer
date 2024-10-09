package searchengine.services.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.searcher.analyzer.SiteIndexingImpl;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class LemmaServiceIT extends BaseTest {

    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final SiteIndexingImpl indexing;
    private static final String EXIST_SITE_NAME = "Sendel.ru";

    @Autowired
    public LemmaServiceIT(LemmaService lemmaService, LemmaRepository lemmaRepository, SiteRepository siteRepository, SiteIndexingImpl indexing) {
        this.lemmaService = lemmaService;
        this.lemmaRepository = lemmaRepository;
        this.siteRepository = siteRepository;
        this.indexing = indexing;
    }

<<<<<<< HEAD
    @Test
    @DisplayName("Testing the create batch lemma method")
    void createBatch(){
        indexing.startSitesIndexing();
        Optional<Site> mayBeSite = siteRepository.findSiteByName(EXIST_SITE_NAME);
        Assertions.assertTrue(mayBeSite.isPresent());

        Site site = mayBeSite.get();
        ExecutorService threadPool = Executors.newCachedThreadPool();

        LemmaCreatorTaskFactory factory = new LemmaCreatorTaskFactory();
        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                site.getPages(), factory, new ConcurrentHashMap<>());
        LemmaCreatorTask task = factory.createTask(lemmaCreatorContext,new ConcurrentHashMap<>());

        List<Map<Page, Map<Lemma, Integer>>> lemmaList = new ArrayList<>();
        threadPool.submit(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            Map<Page, Map<Lemma, Integer>> pageAndLemmas = forkJoinPool.invoke(task);
            lemmaList.add(pageAndLemmas);
            forkJoinPool.shutdown();
        });

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Map<Page, Map<Lemma, Integer>> pageAndLemmas : lemmaList) {
            for(Map.Entry<Page,Map<Lemma,Integer>> lemmasAndCountByPage : pageAndLemmas.entrySet()){
                Page page = lemmasAndCountByPage.getKey();
                Map<Lemma, Integer> lemmasAndCountByPageValue = lemmasAndCountByPage.getValue();
                for(Map.Entry<Lemma,Integer> entry : lemmasAndCountByPageValue.entrySet()){
                    Lemma lemma = entry.getKey();
                    Integer countByPage = entry.getValue();
                    Lemma savedLemma = lemmaRepository.save(lemma);
                    Index index = new Index(page, savedLemma, (float) countByPage);
                }
            }
        }


    }
=======
//    @Test
//    @DisplayName("Testing the create batch lemma method")
//    void createBatch(){
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
//        List<Lemma> batch = lemmaService.createBatch(lemmaList);
//        for (Lemma lemma : batch){
//            assertThat(lemma.getId()).isNotNull();
//        }
//    }
>>>>>>> 0b2c17b (Changing the method of finding lemmas and indexes)
}
