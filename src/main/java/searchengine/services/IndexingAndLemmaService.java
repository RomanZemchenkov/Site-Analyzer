package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.aop.annotation.LuceneInit;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.page.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.service.IndexService;
import searchengine.services.searcher.analyzer.Indexing;
import searchengine.services.service.LemmaService;
import searchengine.services.dto.page.CreatedPageInfoDto;
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
    private final IndexService indexService;
    private final StatisticRepository statisticRepository;

    @LuceneInit
    public void startIndexingAndCreateLemma() {
        indexingService.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> allSites = getAllSites();
        List<List<Lemma>> lemmas = lemmaListCreate(allSites);

        /*
        По какой-то причине, если ставить Propagation.REQUIRES_NEW - не все леммы получают свой id
        Если же убрать и использовать, получается, обычную реализацию, которая, вроде как, использует существующую транзацию
        всё будет отлично.
        ---
        Отбой. К сожалению, это работало только один день и я не понимаю, с чем это связано :с
         */
        for (List<Lemma> lemmaList : lemmas) {
            System.out.println("Количество лемм  перед сохранением: " + lemmaList.size());
            lemmaService.createBatch(lemmaList);
        }
        System.out.println("Леммы созданы и сохранены");
        /*
        Этот метод специально сделан для того, чтобы дать всем леммам id
        Очень хотелось бы его убрать, но я не понимаю, что я не так делаю при сохранении.
        ---
        Заменил его на получение уже существующей леммы из базы данные в классе IndexService
         */

//        GlobalVariables.PAGE_AND_LEMMAS_WITH_COUNT.forEach((Page p, HashMap<Lemma, Integer> map) -> {
//            for (Map.Entry<Lemma, Integer> entry : map.entrySet()) {
//                Lemma lemma = entry.getKey();
//                if (lemma.getId() == null) {
//                    lemmaRepository.saveAndFlush(lemma);
//                    System.out.println(lemma);
//                }
//            }
//        });

        indexCreate();
    }


    @LuceneInit
    public void startIndexingAndCreateLemmaForOnePage(String searchedUrl) {
        CreatedPageInfoDto infoDto = indexingService.onePageIndexing(searchedUrl);
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
            System.out.println("Количество лемм для сайта: " + lemmaList.size());
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
        System.out.println("Очистка информации");
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
