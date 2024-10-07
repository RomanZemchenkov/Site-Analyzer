package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.aop.annotation.LuceneInit;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.page.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.service.IndexService;
import searchengine.services.searcher.analyzer.IndexingImpl;
import searchengine.services.service.LemmaService;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IndexingAndLemmaService {

    private final IndexingImpl indexingService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaCreatorTaskFactory factory;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final StatisticRepository statisticRepository;

    @LuceneInit
    @CheckTimeWorking
    public void startIndexingAndCreateLemma() {
        indexingService.startSitesIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> allSites = getAllSites();
        List<List<Lemma>> lemmas = lemmaListCreate(allSites);

        for (List<Lemma> lemmaList : lemmas) {
            lemmaService.createBatch(lemmaList);
        }

        indexCreate();
    }

    @LuceneInit
    public void startIndexingAndCreateLemmaForOnePage(String searchedUrl) {
        FindPageDto infoDto = indexingService.startPageIndexing(searchedUrl);
        Site site = infoDto.getSite();
        Page page = infoDto.getSavedPage();

        List<Lemma> lemmas = lemmaCreate(site, List.of(page));

        lemmaService.checkExistAndSaveOrUpdate(lemmas, site);

        indexCreate();
    }

    private List<List<Lemma>> lemmaListCreate(List<Site> allSites) {
        GlobalVariables.LEMMA_CREATING_STARTED = true;


        List<List<Lemma>> resultsLemmas = new ArrayList<>();

        for (Site site : allSites) {
            List<Lemma> lemmaList = lemmaCreate(site, site.getPages());
            resultsLemmas.add(lemmaList);
        }

        return resultsLemmas;
    }

    private List<Lemma> lemmaCreate(Site site, List<Page> pages) {
        GlobalVariables.LEMMA_CREATING_STARTED = true;
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        LemmaCreatorTask task = taskCreate(site, pages);

        Future<List<Lemma>> futureResult = futureLemmaCreate(threadPool, task);

        List<Lemma> lemmasList;
        try {
            lemmasList = futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка при создании лемм", e);
        }

        threadPool.shutdown();

        try {
            if (!threadPool.awaitTermination(100L, TimeUnit.MINUTES)) {
                System.err.println("Потоки не завершились за отведенное время");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ожидание завершения потоков было прервано", e);
        }

        return lemmasList;
    }

    private Future<List<Lemma>> futureLemmaCreate(ExecutorService threadPool, LemmaCreatorTask task) {
        return threadPool.submit(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            List<Lemma> lemmaList = forkJoinPool.invoke(task);
            forkJoinPool.shutdown();
            return lemmaList;
        });
    }

    private LemmaCreatorTask taskCreate(Site site, List<Page> pages) {
        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                new ConcurrentLinkedDeque<>(pages), factory, new ConcurrentHashMap<>());
        return factory.createTask(lemmaCreatorContext);
    }

    private void indexCreate() {
        GlobalVariables.INDEX_CREATING_STARTED = true;
        GlobalVariables.LEMMA_CREATING_STARTED = false;

        indexService.createIndex();
        statisticRepository.writeStatistics();
        GlobalVariables.INDEX_CREATING_STARTED = false;
        GlobalVariables.PAGE_AND_LEMMAS_WITH_COUNT.clear();
        GlobalVariables.COUNT_OF_LEMMAS.set(0);
    }


    @Transactional(readOnly = true)
    public List<Site> getAllSites() {
        Set<String> siteNames = indexingService.getNamesAndSites().keySet();
        List<Site> allSites = siteRepository.findAllByName(siteNames);
                /*
        Не понимаю, почему если вызывать метод findAllByName напрямую из тестового класса, то сайты загрузятся вместе со страницами
        Если же это делать через ApiControllerTest - страниц у сайтов не будет
         */
        for (Site site : allSites) {
            site.setPages(pageRepository.findAllBySite(site));
        }
        return allSites;
    }

}
