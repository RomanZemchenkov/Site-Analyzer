package searchengine.services.searcher.lemma;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.searcher.analyzer.Indexing;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LemmaCreatorTaskTest extends BaseTest {

    private final LemmaCreatorTaskFactory factory;
    private final SiteRepository siteRepository;
    private final Indexing service;
    private static final String[] SITES_NAME = {"Sendel.ru","ItDeti.ru"};

    @Autowired
    public LemmaCreatorTaskTest(LemmaCreatorTaskFactory factory, SiteRepository siteRepository, Indexing service) {
        this.factory = factory;
        this.siteRepository = siteRepository;
        this.service = service;
    }

    @Test
    @DisplayName("Testing the creating lemma for one site")
    void lemmaCreatorTaskForOneSiteTest() {
        service.startIndexing();
        Optional<Site> siteByName = siteRepository.findSiteByName(SITES_NAME[0]);
        Assertions.assertTrue(siteByName.isPresent());

        Site site = siteByName.get();

        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                new ConcurrentLinkedDeque<>(site.getPages()), factory, new ConcurrentHashMap<>());

        LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        List<Lemma> resultLemmas = time(() -> assertDoesNotThrow(() -> forkJoinPool.invoke(task)));


        assertThat(resultLemmas).hasSize(1798);
    }

    @Test
    @DisplayName("Create lemma for several sites by multi thread test")
    void lemmaCreatorTaskForSeveralSites() {
        service.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> all = siteRepository.findAll();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<LemmaCreatorTask> taskList = new ArrayList<>();
        for (Site site : all) {
            if (site.getName().equals(SITES_NAME[0]) || site.getName().equals(SITES_NAME[1])) {
                LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                        new ConcurrentLinkedDeque<>(site.getPages()), factory, new ConcurrentHashMap<>());
                LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
                taskList.add(task);
            }
        }

        HashMap<String,Integer> siteNameAndCountOfLemmas = new HashMap<>();
        for (LemmaCreatorTask task : taskList) {
            threadPool.submit(() -> {
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                List<Lemma> lemmas = time(() -> assertDoesNotThrow(() -> forkJoinPool.invoke(task)));
                if(lemmas.get(0).getSite().getName().equals(SITES_NAME[0])){
                    siteNameAndCountOfLemmas.put(SITES_NAME[0],lemmas.size());
                } else if (lemmas.get(0).getSite().getName().equals(SITES_NAME[1])){
                    siteNameAndCountOfLemmas.put(SITES_NAME[1],lemmas.size());
                }
                forkJoinPool.shutdown();
            });
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(Map.Entry<String,Integer> entry : siteNameAndCountOfLemmas.entrySet()){
            String siteName = entry.getKey();
            Integer count = entry.getValue();
            if(siteName.equals(SITES_NAME[0])){
                assertThat(count).isEqualTo(1798);
            } else if (siteName.equals(SITES_NAME[1])){
                assertThat(count).isEqualTo(2732);
            }
        }

    }

    static <T> T time(Supplier<T> supplier) {
        long start = System.currentTimeMillis();
        T t = supplier.get();
        long finish = System.currentTimeMillis();
        System.out.println("Метод отработал за: " + (finish - start));
        return t;
    }
}
