package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.aop.annotation.CheckQuery;
import searchengine.aop.annotation.CheckIndexingWork;
import searchengine.aop.annotation.CheckSiteExist;
import searchengine.aop.annotation.LuceneInit;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.index.IndexRepository;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.dao.repository.page.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.dto.page.ShowPageDto;
import searchengine.services.parser.LuceneMorphologyGiver;
import searchengine.services.parser.lemma.TextToLemmaParserImpl;
import searchengine.services.parser.snippet.SnippetCreator;
import searchengine.services.parser.snippet.SnippetCreatorImpl;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerImpl;
import searchengine.web.entity.SearchResponse;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;
    private static SearchParametersDto prevQueryParameters;
    private static List<ShowPageDto> prevSearchResult;
    private static final float PERCENT_OF_TOTAL_COUNT = 0.95f;


    @LuceneInit
    @CheckQuery
    @CheckIndexingWork
    @CheckSiteExist
    public SearchResponse search(SearchParametersDto searchedTextAndParameters) {
        if(checkQueriesMatch(searchedTextAndParameters)){
            return createResponse(searchedTextAndParameters,prevSearchResult);
        }
        prevQueryParameters = searchedTextAndParameters;
        String searchedQuery = searchedTextAndParameters.getQuery();
        String mayBeUrl = searchedTextAndParameters.getUrl();

        Set<String> lemmas = parseToLemmas(searchedQuery);

        List<Site> usesSites = findUsesSites(mayBeUrl);

        List<ShowPageDto> showPagesList = new ArrayList<>();
        for (Site oneSite : usesSites) {
            showPagesList.addAll(findSitesPages(oneSite, lemmas));
        }
        showPagesList.sort((o1, o2) -> o2.getRelevance().compareTo(o1.getRelevance()));
        prevSearchResult = showPagesList;
        return createResponse(searchedTextAndParameters,showPagesList);
    }


    public void clearPrevInformation(){
        prevQueryParameters = null;
        prevSearchResult = null;
    }

    private SearchResponse createResponse(SearchParametersDto dto, List<ShowPageDto> searchResult){
        String limit = dto.getLimit();
        String offset = dto.getOffset();
        List<ShowPageDto> offsetList = new ArrayList<>();
        int limitByInt = Integer.parseInt(limit);
        int offsetByInt = Integer.parseInt(offset);
        int lastPageIndex = Math.min(limitByInt + offsetByInt,searchResult.size());
        for(int i = offsetByInt; i < lastPageIndex; i++){
            offsetList.add(searchResult.get(i));
        }
        return new SearchResponse("true", searchResult.size(), offsetList);
    }

    private boolean checkQueriesMatch(SearchParametersDto currentQueryParameters){
        if(prevQueryParameters != null){
            return prevQueryParameters.equals(currentQueryParameters);
        }
        return false;
    }

    private List<ShowPageDto> findSitesPages(Site site, Set<String> lemmas) {
        List<Lemma> existLemmasForOneSite = findExistLemmas(site, lemmas);
        List<String> suitableLemmas = new ArrayList<>();
        Map<Page, List<Index>> suitablePagesForOneSite = findSuitablePages(existLemmasForOneSite, suitableLemmas);
        List<ShowPageDto> suitablePagesList = new ArrayList<>();
        if (!suitablePagesForOneSite.isEmpty()) {
            Map<Float, Page> pageAndMaxRelevance = new HashMap<>();
            float maxRelevanceBySite = checkMaxRelevance(suitablePagesForOneSite, pageAndMaxRelevance);
            suitablePagesList = executorShowPageCreated(pageAndMaxRelevance, suitableLemmas, maxRelevanceBySite);
        }
        return suitablePagesList;
    }

    @Deprecated
    private HashMap<Integer, List<ShowPageDto>> getByLimitAndOffset(List<ShowPageDto> showPages, String limit) {
        int maxOfOnePage = Integer.parseInt(limit);
        HashMap<Integer, List<ShowPageDto>> pageable = new HashMap<>();
        List<ShowPageDto> tempPagesList = new ArrayList<>();
        int pageCounter = 0;
        for (ShowPageDto showPage : showPages) {
            tempPagesList.add(showPage);
            if (tempPagesList.size() % maxOfOnePage == 0) {
                pageable.put(pageCounter++, tempPagesList);
                tempPagesList = new ArrayList<>();
            }
        }
        if (!tempPagesList.isEmpty()) {
            pageable.put(pageCounter, tempPagesList);
        }
        return pageable;
    }


    private Set<String> parseToLemmas(String searchedQuery) {
        return new TextToLemmaParserImpl().parse(searchedQuery).keySet();
    }

    private List<Site> findUsesSites(String mayBeUrl) {
        if (mayBeUrl == null || mayBeUrl.isBlank()) {
            return siteRepository.findAll();
        } else {
            return List.of(siteRepository.findSiteByUrl(mayBeUrl).get());
        }
    }

    private List<Lemma> findExistLemmas(Site useSite, Set<String> lemmas) {
        long countOfPagesBySite = pageRepository.findCountOfPagesBySite(useSite);
        int maxLemmaFrequency = (int) (countOfPagesBySite * PERCENT_OF_TOTAL_COUNT);
        return lemmaRepository.findAllBySiteIdAndLemmas(useSite, lemmas)
                .stream()
                .filter(lemma -> lemma.getFrequency() <= maxLemmaFrequency)
                .sorted((l1, l2) -> l1.getFrequency().compareTo(l2.getFrequency()))
                .toList();
    }

    private Map<Page, List<Index>> findSuitablePages(List<Lemma> targetLemmas, List<String> suitableLemmas) {
        int counter = 0;
        int max = 2;
        Map<Page, List<Index>> suitablePages = new HashMap<>();
        for (Lemma targetLemma : targetLemmas) {
            if (counter == max || (counter > 0 && suitablePages.isEmpty())) {
                break;
            }
            suitableLemmas.add(targetLemma.getLemma());
            if (suitablePages.isEmpty()) {
                suitablePages = indexRepository.findAllIndexesWithPageByLemmas(targetLemma)
                        .stream()
                        .collect(Collectors.toMap(Index::getPage, List::of));
                continue;
            }

            Map<Page, Index> currentPages = indexRepository.findAllIndexesWithPageByLemmas(targetLemma)
                    .stream()
                    .collect(Collectors.toMap(Index::getPage, index -> index));

            suitablePages = getUpdatedSuitablePages(suitablePages, currentPages);

            counter++;
        }

        return suitablePages;
    }

    private static Map<Page, List<Index>> getUpdatedSuitablePages(Map<Page, List<Index>> suitablePages, Map<Page, Index> currentPages) {
        Map<Page, List<Index>> updatedSuitablePages = new HashMap<>();
        for (Map.Entry<Page, List<Index>> entry : suitablePages.entrySet()) {
            Page suitablePage = entry.getKey();
            for (Map.Entry<Page, Index> currentEntry : currentPages.entrySet()) {
                Page currentPage = currentEntry.getKey();
                if (suitablePage.equals(currentPage)) {
                    List<Index> currentSuitableIndexes = entry.getValue();
                    List<Index> updateSuitableIndexes = new ArrayList<>(currentSuitableIndexes);
                    Index currentIndex = currentEntry.getValue();
                    updateSuitableIndexes.add(currentIndex);
                    updatedSuitablePages.put(suitablePage, updateSuitableIndexes);
                }
            }
        }
        return updatedSuitablePages;
    }

    private float checkMaxRelevance(Map<Page, List<Index>> suitablePages, Map<Float, Page> pagesAndMaxRelevance) {
        float maxRelevance = Integer.MIN_VALUE;
        for (Map.Entry<Page, List<Index>> entry : suitablePages.entrySet()) {
            Page currentPage = entry.getKey();
            List<Index> currentIndexes = entry.getValue();
            Float maxRankByCurrentPage = currentIndexes.stream()
                    .map(Index::getRank)
                    .reduce(Float::sum)
                    .get();
            maxRelevance = Math.max(maxRankByCurrentPage, maxRelevance);
            pagesAndMaxRelevance.put(maxRankByCurrentPage, currentPage);
        }
        return maxRelevance;

    }

    private List<ShowPageDto> executorShowPageCreated(Map<Float, Page> pagesAndRelevance, List<String> suitableLemmas, float maxRelevanceBySite) {
        List<Callable<ShowPageDto>> callablesList = pagesAndRelevance.entrySet()
                .stream()
                .map(set -> (Callable<ShowPageDto>) () -> createShowPageDto(set.getValue(), suitableLemmas, set.getKey(), maxRelevanceBySite))
                .toList();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<ShowPageDto> preparedShowPageDto;
        try {
            List<Future<ShowPageDto>> futuresShowPagesDto = threadPool.invokeAll(callablesList);
            preparedShowPageDto = createPreparedShowPageDtoFromFuture(futuresShowPagesDto);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(120L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return preparedShowPageDto;
    }


    private List<ShowPageDto> createPreparedShowPageDtoFromFuture(List<Future<ShowPageDto>> futuresShowPagesDto){
        return futuresShowPagesDto.stream()
                .map(fut -> {
                    try {
                        return fut.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }


    private ShowPageDto createShowPageDto(Page page, List<String> suitableLemmas, float maxRelevanceByPage, float maxRelevanceBySite) {
        String pathToPage = page.getPath();
        String content = page.getContent();
        Site site = page.getSite();
        String siteName = site.getName();
        String siteUrl = site.getUrl();

        RussianLuceneMorphology russianLuceneMorphology = LuceneMorphologyGiver.get();
        SnippetCreator snippetCreatorTask = new SnippetCreatorImpl(suitableLemmas, russianLuceneMorphology);
        LuceneMorphologyGiver.returnLucene(russianLuceneMorphology);

        String snippet = snippetCreatorTask.createSnippet(content);

        String pageTitle = new PageAnalyzerImpl().searchPageTitle(content);
        Float relativeRelevance = maxRelevanceByPage / maxRelevanceBySite;
        String relativeRelevanceByString = String.format("%.4f", relativeRelevance);

        return new ShowPageDto(pathToPage, pageTitle, snippet, relativeRelevanceByString, siteName, siteUrl);
    }
}
