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
import searchengine.services.searcher.analyzer.SiteSiteIndexingImpl;
import searchengine.services.service.LemmaService;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static searchengine.services.GlobalVariables.COUNT_OF_LEMMAS;
import static searchengine.services.GlobalVariables.INDEXING_STARTED;
import static searchengine.services.GlobalVariables.INDEX_CREATING_STARTED;
import static searchengine.services.GlobalVariables.LEMMA_CREATING_STARTED;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private final SiteSiteIndexingImpl indexingService;
    private final LemmaService lemmaService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexService indexService;
    private final StatisticRepository statisticRepository;

    @LuceneInit
    @CheckTimeWorking
    public void startIndexingAndCreateLemma() {
        INDEXING_STARTED.set(true);
        indexingService.startSitesIndexing();
        LEMMA_CREATING_STARTED.set(true);
        INDEXING_STARTED.set(false);

        List<Site> allSites = getAllSites();
        List<Map<Page, Map<Lemma, Integer>>> lemmas = lemmaService.lemmasCreator(allSites);
        INDEX_CREATING_STARTED.set(true);
        LEMMA_CREATING_STARTED.set(false);

        saveLemmasAndIndexesBySites(lemmas);
        INDEX_CREATING_STARTED.set(false);
        statisticRepository.writeStatistics();
        COUNT_OF_LEMMAS.set(0);
    }

    @LuceneInit
    @CheckTimeWorking
    public void startIndexingAndCreateLemmaForOnePage(String searchedUrl) {
        FindPageDto infoDto = indexingService.startPageIndexing(searchedUrl);
        Site site = infoDto.getSite();
        Page page = infoDto.getSavedPage();

        Map<Page, Map<Lemma, Integer>> taskResult = lemmaService.lemmaCreator(site, List.of(page));
        List<Lemma> alreadyExistLemmas = lemmaService.findAllBySite(site);
        saveLemmasAndIndexesByPage(taskResult, alreadyExistLemmas);
        statisticRepository.writeStatistics();
    }


    private void saveLemmasAndIndexesBySites(List<Map<Page, Map<Lemma, Integer>>> lemmas){
        for(Map<Page, Map<Lemma, Integer>> pagesAndLemmas : lemmas){
            List<Lemma> alreadySavedLemmas = new ArrayList<>();
            saveLemmasAndIndexesByPage(pagesAndLemmas, alreadySavedLemmas);
        }
    }

    private void saveLemmasAndIndexesByPage(Map<Page, Map<Lemma, Integer>> taskResult, List<Lemma> alreadyExistLemmas) {
        for (Map.Entry<Page, Map<Lemma, Integer>> entry : taskResult.entrySet()) {
            Page currentPage = entry.getKey();
            Map<Lemma, Integer> lemmasAndCounts = entry.getValue();
            List<Lemma> lemmasForPage = lemmaService.saveBatchLemmas(lemmasAndCounts, alreadyExistLemmas);
            indexService.saveBatchIndexes(currentPage,lemmasForPage,lemmasAndCounts);
        }
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
