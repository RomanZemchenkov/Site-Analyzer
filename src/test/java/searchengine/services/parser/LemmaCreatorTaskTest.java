package searchengine.services.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class LemmaCreatorTaskTest extends BaseTest {

    private final LemmaCreatorTaskFactory factory;
    private final SiteRepository siteRepository;
    private final IndexingService service;

    @Autowired
    public LemmaCreatorTaskTest(LemmaCreatorTaskFactory factory, SiteRepository siteRepository, IndexingService service) {
        this.factory = factory;
        this.siteRepository = siteRepository;
        this.service = service;
    }

    @Test
    @DisplayName("Тестирование получения лемм для одного сайта")
    void lemmaCreatorTaskForOneSiteTest(){
        service.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> all = siteRepository.findAll();
        Site sendel = all.stream().filter(site -> site.getName().equals("Sendel.ru")).findAny().get();

        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(sendel,
                new ConcurrentLinkedDeque<>(sendel.getPages()),factory, new ConcurrentHashMap<>());
        LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        List<Lemma> resultLemmas = forkJoinPool.invoke(task);
        resultLemmas.sort(Comparator.comparing(Lemma::getFrequency).reversed());
        System.out.println(resultLemmas);
        System.out.println(resultLemmas.size() );

    }

    @Test
    @DisplayName("Create lemma for several sites by multi thread test")
    void lemmaCreatorTaskForSeveralSites(){
        service.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> all = siteRepository.findAll();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<LemmaCreatorTask> taskList = new ArrayList<>();
        for(Site site : all){
            if(site.getName().equals("Sendel.ru")){
                LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                        new ConcurrentLinkedDeque<>(site.getPages()),factory, new ConcurrentHashMap<>());
                LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
                taskList.add(task);
            }
            if(site.getName().equals("ItDeti.ru")){
                LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                        new ConcurrentLinkedDeque<>(site.getPages()),factory, new ConcurrentHashMap<>());
                LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
                taskList.add(task);
            }
        }
        List<List<Lemma>> results = new ArrayList<>();
        for(LemmaCreatorTask task : taskList){
            threadPool.submit(() ->{
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                List<Lemma> lemmasList = forkJoinPool.invoke(task);
                lemmasList.sort(Comparator.comparing(Lemma::getFrequency).reversed());
                results.add(lemmasList);
                System.out.println("Количество лемм для сайта: " + lemmasList.size());

                forkJoinPool.shutdown();
            });
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L,TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(results);

    }
}
