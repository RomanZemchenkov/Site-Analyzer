package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.service.IndexService;
import searchengine.services.searcher.analyzer.Indexing;
import searchengine.services.service.LemmaService;
import searchengine.services.dto.page.CreatedPageInfoDto;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingAndLemmaService {

    private final Indexing indexingService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaCreatorTaskFactory factory;
    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final IndexService indexService;
    private final StatisticRepository statisticRepository;


    public void startIndexingAndCreateLemma() {
        indexingService.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> allSites = getAllSites();
        List<List<Lemma>> lemmas = lemmaCreate(allSites);

        for (List<Lemma> lemmaList : lemmas) {
            lemmaService.createBatch(lemmaList);
        }


        GlobalVariables.PAGE_AND_LEMMAS_WITH_COUNT.forEach((Page p, HashMap<Lemma, Integer> map) -> {
            for (Map.Entry<Lemma, Integer> entry : map.entrySet()) {
                Lemma lemma = entry.getKey();
                if (lemma.getId() == null) {
                    lemmaRepository.saveAndFlush(lemma);
                    System.out.println(lemma);
                }
            }
        });

        indexCreate();
    }


    public void startIndexingAndCreateLemmaForOnePage(FindPageDto dto) {
        CreatedPageInfoDto infoDto = indexingService.onePageIndexing(dto);
        Site site = infoDto.getSite();
        Page savedPage = infoDto.getSavedPage();

        LemmaCreatorTask task = taskCreate(site, List.of(savedPage));
        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<Lemma> results = lemmaCreate(threadPool, task);

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        lemmaService.createBatch(results);

        indexCreate();
    }

    private List<List<Lemma>> lemmaCreate(List<Site> allSites) {
        GlobalVariables.LEMMA_CREATING_STARTED = true;
        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<LemmaCreatorTask> taskList = new ArrayList<>();
        for (Site site : allSites) {
            LemmaCreatorTask task = taskCreate(site, site.getPages());
            taskList.add(task);
        }

        List<List<Lemma>> results = new ArrayList<>();
        for (LemmaCreatorTask task : taskList) {
            results.add(lemmaCreate(threadPool, task));
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private List<Lemma> lemmaCreate(ExecutorService threadPool, LemmaCreatorTask task) {
        List<Lemma> lemmaList = new ArrayList<>();
        threadPool.submit(() -> {
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            lemmaList.addAll(forkJoinPool.invoke(task));
            System.out.println("Количество лемм для сайта: " + lemmaList.size());

            forkJoinPool.shutdown();
        });
        return lemmaList;
    }

    private LemmaCreatorTask taskCreate(Site site, List<Page> pages) {
        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                new ConcurrentLinkedDeque<>(pages), factory, new ConcurrentHashMap<>());
        return factory.createTask(lemmaCreatorContext);
    }

    private void indexCreate(){
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
