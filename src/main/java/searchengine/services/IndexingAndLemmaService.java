package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.aop.annotation.LuceneInit;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.service.IndexService;
import searchengine.services.searcher.analyzer.IndexingImpl;
import searchengine.services.service.LemmaService;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingAndLemmaService {

    private final IndexingImpl indexingService;
    private final LemmaCreatorTaskFactory factory;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final StatisticRepository statisticRepository;

    @LuceneInit
    @CheckTimeWorking
    public void startIndexingAndCreateLemma() {
        HashMap<Site, List<Page>> sitesAndPages = indexingService.startSitesIndexing();
        System.out.println("Индексация и запись окончена");
        List<Map<Page, Map<Lemma, Integer>>> lemmas = lemmaListCreate(sitesAndPages);

        for (Map<Page, Map<Lemma, Integer>> pageAndLemmas : lemmas) {
            Set<Lemma> allLemmasBySite = new HashSet<>();
            Set<Lemma> alreadySavedLemmas = new HashSet<>();
            List<Index> allIndexesBySite = new ArrayList<>();
            for (Map.Entry<Page, Map<Lemma, Integer>> entry : pageAndLemmas.entrySet()) {
                Page page = entry.getKey();
                Map<Lemma, Integer> value = entry.getValue();
                for (Map.Entry<Lemma, Integer> lemmasAndCountByPage : value.entrySet()) {
                    Lemma lemma = lemmasAndCountByPage.getKey();
                    if (!alreadySavedLemmas.contains(lemma)) {
                        allLemmasBySite.add(lemma);
                        alreadySavedLemmas.add(lemma);
                    }
                    Integer countByPage = lemmasAndCountByPage.getValue();
                    Index indexForSave = new Index(page, lemma, (float) countByPage);
                    allIndexesBySite.add(indexForSave);
                }
                lemmaService.createBatch(allLemmasBySite.stream().toList());
                allLemmasBySite = new HashSet<>();
            }
            System.out.println("Всего лемм сохранено: " + alreadySavedLemmas.size());
            System.out.println("Начинаю сохранять");
            indexCreate(allIndexesBySite);
            System.out.println("Всё сохранил ");
        }
        System.out.println("Леммы созданы и сохранены");
    }

//    private void saveLemmaAndIndexes(Page page,Map<Lemma,Integer> lemmasAndCountByPage){
//        List<Lemma> lemmasForSave = new ArrayList<>();
//        List<Index> indexesForSave = new ArrayList<>();
//        lemmasAndCountByPage.entrySet()
//                .stream()
//                .forEach((key) -> {
//                    Lemma lemma = key.getKey();
//                    Integer countByPage = key.getValue();
//                    lemmasForSave.add(lemma);
//                    Index indexForSave = new Index(page, lemma, (float) countByPage);
//                    indexesForSave.add(indexForSave);
//                });
//        lemmaService.createBatch(lemmasForSave);
//        indexRepository.batchSave(indexesForSave);
//        System.out.println("Всё сохранил " + Thread.currentThread().getName());
//    }

    @LuceneInit
    public void startIndexingAndCreateLemmaForOnePage(String searchedUrl) {
        FindPageDto infoDto = indexingService.startPageIndexing(searchedUrl);
        Site site = infoDto.getSite();
        Page page = infoDto.getSavedPage();

        Map<Page, Map<Lemma, Integer>> pageAndLemmas = lemmaCreate(site, List.of(page));

        lemmaService.checkExistAndSaveOrUpdate(null, site);

        indexCreate(null);
    }

    private List<Map<Page, Map<Lemma, Integer>>> lemmaListCreate(HashMap<Site, List<Page>> sitesAndPages) {
        GlobalVariables.LEMMA_CREATING_STARTED = true;

        return sitesAndPages
                .entrySet()
                .stream()
                .map(part -> lemmaCreate(part.getKey(), part.getValue()))
                .toList();
    }

    private Map<Page, Map<Lemma, Integer>> lemmaCreate(Site site, List<Page> pages) {
        GlobalVariables.LEMMA_CREATING_STARTED = true;

        LemmaCreatorTask task = taskCreate(site, pages);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Map<Page, Map<Lemma, Integer>> taskResult = forkJoinPool.invoke(task);

        forkJoinPool.shutdown();

        try {
            if (!forkJoinPool.awaitTermination(100L, TimeUnit.MINUTES)) {
                System.err.println("Потоки не завершились за отведенное время");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ожидание завершения потоков было прервано", e);
        }

        LemmaCreatorContext context = task.getContext();
        ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();
        countOfLemmas.forEach(Lemma::setFrequency);
        System.out.println("Всего лемм: " + countOfLemmas.size());

        return taskResult;
    }


    private LemmaCreatorTask taskCreate(Site site, List<Page> pages) {
        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                pages, factory, new ConcurrentHashMap<>());
        return factory.createTask(lemmaCreatorContext, new ConcurrentHashMap<>());
    }

    private void indexCreate(List<Index> allIndexesBySite) {
        GlobalVariables.INDEX_CREATING_STARTED = true;
        GlobalVariables.LEMMA_CREATING_STARTED = false;

        indexService.createIndex(allIndexesBySite);
        statisticRepository.writeStatistics();
        GlobalVariables.INDEX_CREATING_STARTED = false;
        System.out.println("Очистка информации");
        GlobalVariables.COUNT_OF_LEMMAS.set(0);
    }

}
