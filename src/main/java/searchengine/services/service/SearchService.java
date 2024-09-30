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
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.dto.page.ShowPageDto;
import searchengine.services.parser.LuceneMorphologyGiver;
import searchengine.services.parser.lemma.TextToLemmaParserImpl;
import searchengine.services.parser.snippet.SnippetCreator;
import searchengine.services.parser.snippet.SnippetCreatorImpl;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerImpl;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private static final float PERCENT_OF_TOTAL_COUNT = 0.95f;


    @LuceneInit
    @CheckQuery
    @CheckIndexingWork
    @CheckSiteExist
    public HashMap<Integer, List<ShowPageDto>> search(SearchParametersDto searchedTextAndParameters) {
        String searchedQuery = searchedTextAndParameters.getQuery();
        String mayBeUrl = searchedTextAndParameters.getUrl();
        String limit = searchedTextAndParameters.getLimit();

        Set<String> lemmas = timeForLemmaCreate(() -> parseToLemmas(searchedQuery));

        List<Site> usesUrls = timeForSiteFind(() -> findUsesUrls(mayBeUrl));

        List<ShowPageDto> showPagesList = new ArrayList<>();
        timeForShowPageCreate(() -> {
            for (Site oneSite : usesUrls) {
                List<Lemma> existLemmasForOneSite = findExistLemmas(oneSite, lemmas);
                List<String> suitableLemmas = new ArrayList<>();
                Map<Page, List<Index>> suitablePagesForOneSite = findSuitablePages(existLemmasForOneSite, suitableLemmas);
                if (!suitablePagesForOneSite.isEmpty()) {
                    List<ShowPageDto> showPagesBySite = checkRelevance(suitablePagesForOneSite, suitableLemmas);
                    showPagesList.addAll(showPagesBySite);
                }
            }
        });
        showPagesList.sort((o1, o2) -> o2.getRelevance().compareTo(o1.getRelevance()));
        return getByLimitAndOffset(showPagesList, limit);
    }

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

    private List<Site> findUsesUrls(String mayBeUrl) {
        if (mayBeUrl == null || mayBeUrl.isBlank()) {
            return siteRepository.findAll();
        } else {
            return List.of(siteRepository.findSiteByUrl(mayBeUrl).get());
        }
    }

    private List<Lemma> findExistLemmas(Site useSite, Set<String> lemmas) {
        int maxLemmaFrequency = (int) (useSite.getPages().size() * PERCENT_OF_TOTAL_COUNT);
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
                suitablePages = indexRepository.findAllByLemma(targetLemma)
                        .stream()
                        .collect(Collectors.toMap(Index::getPage, List::of));
                continue;
            }

            Map<Page, Index> currentPages = indexRepository.findAllByLemma(targetLemma)
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

    private List<ShowPageDto> checkRelevance(Map<Page, List<Index>> suitablePages, List<String> suitableLemmas) {
        float maxRelevance = Integer.MIN_VALUE;
        Map<Float, ShowPageDto> pageAndRelevance = new HashMap<>();
        for (Map.Entry<Page, List<Index>> entry : suitablePages.entrySet()) {
            Page currentPage = entry.getKey();
            List<Index> currentIndexes = entry.getValue();
            Float maxRankByCurrentPage = currentIndexes.stream()
                    .map(Index::getRank)
                    .reduce(Float::sum)
                    .get();

            ShowPageDto showPageDto = createShowPageDto(currentPage, suitableLemmas);
            maxRelevance = Math.max(maxRankByCurrentPage, maxRelevance);
            Site currentSite = currentPage.getSite();
            String siteName = currentSite.getName();
            String siteUrl = currentSite.getUrl();
            showPageDto.setSite(siteUrl);
            showPageDto.setSiteName(siteName);
            pageAndRelevance.put(maxRankByCurrentPage, showPageDto);
        }

        Map<Float, ShowPageDto> showPageByRelevance = new TreeMap<>(Float::compareTo);
        for (Map.Entry<Float, ShowPageDto> entry : pageAndRelevance.entrySet()) {
            Float currentRelevance = entry.getKey();
            Float relativeRelevance = currentRelevance / maxRelevance;
            ShowPageDto currentPage = entry.getValue();
            currentPage.setRelevance(String.format("%.4f",relativeRelevance));
            showPageByRelevance.put(relativeRelevance, currentPage);
        }

        List<ShowPageDto> showList = new ArrayList<>();
        for (Map.Entry<Float, ShowPageDto> entry : showPageByRelevance.entrySet()) {
            showList.add(entry.getValue());
        }


        return showList;
    }

    private ShowPageDto createShowPageDto(Page page, List<String> suitableLemmas) {
        String pathToPage = page.getPath();
        String content = page.getContent();
        RussianLuceneMorphology russianLuceneMorphology = LuceneMorphologyGiver.get();
        SnippetCreator snippetCreatorTask = new SnippetCreatorImpl(suitableLemmas, russianLuceneMorphology);
        LuceneMorphologyGiver.returnLucene(russianLuceneMorphology);
        String snippet = snippetCreatorTask.createSnippet(content);
        String pageTitle = new PageAnalyzerImpl().searchPageTitle(content);
        return new ShowPageDto(pathToPage, pageTitle, snippet);
    }

    static <T> T timeForLemmaCreate(Supplier<T> runnable) {
        long start = System.currentTimeMillis();
        T t = runnable.get();
        long finish = System.currentTimeMillis();
        System.out.println("Леммы созданы за: " + (finish - start));
        return t;
    }

    static <T> T timeForSiteFind(Supplier<T> runnable) {
        long start = System.currentTimeMillis();
        T t = runnable.get();
        long finish = System.currentTimeMillis();
        System.out.println("Сайты найдены за: " + (finish - start));
        return t;
    }

    static void timeForShowPageCreate(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Страницы созданы за: " + (finish - start));
    }
}
