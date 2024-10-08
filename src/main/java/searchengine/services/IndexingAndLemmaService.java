package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.aop.annotation.LuceneInit;
import searchengine.dao.model.Index;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import static searchengine.services.GlobalVariables.COUNT_OF_LEMMAS;
import static searchengine.services.GlobalVariables.INDEXING_STARTED;
import static searchengine.services.GlobalVariables.INDEX_CREATING_STARTED;
import static searchengine.services.GlobalVariables.LEMMA_CREATING_STARTED;

@Service
@RequiredArgsConstructor
public class IndexingAndLemmaService {

    private final IndexingImpl indexingService;
    private final LemmaCreatorTaskFactory factory;
    private final LemmaService lemmaService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexService indexService;
    private final StatisticRepository statisticRepository;

    @LuceneInit
    @CheckTimeWorking
    public void startIndexingAndCreateLemma() {
        INDEXING_STARTED = true;
        indexingService.startSitesIndexing();
        LEMMA_CREATING_STARTED = true;
        INDEXING_STARTED = false;

        List<Site> allSites = getAllSites();
        List<Map<Page, Map<Lemma, Integer>>> lemmas = lemmaListCreate(allSites);
        INDEX_CREATING_STARTED = true;
        LEMMA_CREATING_STARTED = false;

        saveIndexesAndLemmas(lemmas);
        INDEX_CREATING_STARTED = false;

        COUNT_OF_LEMMAS.set(0);
    }

    @LuceneInit
    @CheckTimeWorking
    public void startIndexingAndCreateLemmaForOnePage(String searchedUrl) {
        FindPageDto infoDto = indexingService.startPageIndexing(searchedUrl);
        Site site = infoDto.getSite();
        Page page = infoDto.getSavedPage();

        Map<Page, Map<Lemma, Integer>> taskResult = lemmaCreate(site, List.of(page));
        List<Lemma> alreadyExistLemmas = lemmaService.findAllBySite(site);
        for(Map.Entry<Page,Map<Lemma,Integer>> entry : taskResult.entrySet()){
            Page p = entry.getKey();
            Map<Lemma, Integer> lemmasAndCounts = entry.getValue();
            List<Index> indexList = creatIndexesAndLemmas(p, lemmasAndCounts, alreadyExistLemmas);
            saveIndexes(indexList);
        }
    }

    private void saveIndexesAndLemmas(List<Map<Page, Map<Lemma, Integer>>> lemmas) {
        List<Index> allIndexesBySite = new ArrayList<>();
        for (Map<Page, Map<Lemma, Integer>> pageAndLemmas : lemmas) {
            List<Lemma> alreadySavedLemmas = new ArrayList<>();
            for (Map.Entry<Page, Map<Lemma, Integer>> entry : pageAndLemmas.entrySet()) {
                allIndexesBySite.addAll(creatIndexesAndLemmas(entry.getKey(), entry.getValue(), alreadySavedLemmas));
            }
        }
        saveIndexes(allIndexesBySite);
    }

    private List<Index> creatIndexesAndLemmas(Page page, Map<Lemma, Integer> lemmasAndCounts, List<Lemma> alreadySavedLemmas) {
        List<Lemma> allLemmasForSave = new ArrayList<>();
        List<Index> allIndexesByPage = new ArrayList<>();
        for (Map.Entry<Lemma, Integer> lemmasAndCountByPage : lemmasAndCounts.entrySet()) {
            boolean flag = false;
            Lemma lemma = lemmasAndCountByPage.getKey();
            for(Lemma mayBeExistLemma : alreadySavedLemmas){
                if(mayBeExistLemma.equals(lemma)){
                    lemma = mayBeExistLemma;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                allLemmasForSave.add(lemma);
            }
            Integer countByPage = lemmasAndCountByPage.getValue();
            Index indexForSave = new Index(page, lemma, (float) countByPage);
            allIndexesByPage.add(indexForSave);
        }
        alreadySavedLemmas.addAll(lemmaService.createBatch(allLemmasForSave));
        return allIndexesByPage;
    }

    private void saveIndexes(List<Index> allIndexes){
        indexService.createIndex(allIndexes);
        statisticRepository.writeStatistics();
    }



    private List<Map<Page, Map<Lemma, Integer>>> lemmaListCreate(List<Site> allSites) {
        return allSites
                .stream()
                .map(site -> lemmaCreate(site, site.getPages()))
                .toList();
    }

    private Map<Page, Map<Lemma, Integer>> lemmaCreate(Site site, List<Page> pages) {
        LEMMA_CREATING_STARTED = true;

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

        System.out.println("Количество лемм: " + countOfLemmas.size());
        return taskResult;
    }
    private LemmaCreatorTask taskCreate(Site site, List<Page> pages) {
        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                pages, factory, new ConcurrentHashMap<>());
        return factory.createTask(lemmaCreatorContext, new ConcurrentHashMap<>());
    }


    @Transactional(readOnly = true)
    public List<Site> getAllSites() {
        Set<String> siteNames = indexingService.getNamesAndSites().keySet();
        List<Site> allSites = siteRepository.findAllByName(siteNames);
        for (Site site : allSites) {
            site.setPages(pageRepository.findAllBySite(site));
        }
        return allSites;
    }

}
