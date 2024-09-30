package searchengine.services.service;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class LemmaServiceIT extends BaseTest {

    private final LemmaService lemmaService;
    private final SiteRepository siteRepository;
    private final IndexingImpl indexing;
    private static final String EXIST_SITE_NAME = "Sendel.ru";

    @Autowired
    public LemmaServiceIT(LemmaService lemmaService, SiteRepository siteRepository, IndexingImpl indexing) {
        this.lemmaService = lemmaService;
        this.siteRepository = siteRepository;
        this.indexing = indexing;
    }

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
                new ConcurrentLinkedDeque<>(site.getPages()), factory, new ConcurrentHashMap<>());
        LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);

        List<Lemma> lemmaList = new ArrayList<>();
        threadPool.submit(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            lemmaList.addAll(forkJoinPool.invoke(task));
            forkJoinPool.shutdown();
        });

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<Lemma> batch = lemmaService.createBatch(lemmaList);
        for (Lemma lemma : batch){
            assertThat(lemma.getId()).isNotNull();
        }
    }
}
