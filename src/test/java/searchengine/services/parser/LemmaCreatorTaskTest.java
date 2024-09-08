package searchengine.services.parser;

import org.apache.logging.log4j.util.PropertySource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;
import searchengine.dao.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ForkJoinPool;

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
    @DisplayName("Тестирование получения лемм")
    void lemmaCreatorTaskForOneSiteTest(){
        service.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> all = siteRepository.findAll();
        Site sendel = all.stream().filter(site -> site.getName().equals("Sendel.ru")).findAny().get();

        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(sendel, new ConcurrentLinkedDeque<>(sendel.getPages()),factory);
        LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        List<Lemma> resultLemmas = forkJoinPool.invoke(task);
        resultLemmas.sort(Comparator.comparing(Lemma::getFrequency).reversed());
        System.out.println(resultLemmas);
        System.out.println(resultLemmas.size() );
    }
}
