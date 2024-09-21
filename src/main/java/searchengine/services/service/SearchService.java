package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import searchengine.services.parser.TextToLemmaParser;
import searchengine.services.parser.snippet.SnippetCreator;
import searchengine.services.parser.snippet.SnippetCreatorImpl;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private static final float PERCENT_OF_TOTAL_COUNT = 0.95f;
    private final static List<String> suitableLemmas = new ArrayList<>();


    @LuceneInit
    public List<ShowPageDto> search(SearchParametersDto searchedTextAndParameters) {
        String searchedQuery = searchedTextAndParameters.getQuery();
        String mayBeUrl = searchedTextAndParameters.getUrl();

        Set<String> lemmas = parseToLemmas(searchedQuery);

        List<Site> usesUrls = findUsesUrls(mayBeUrl);

        List<Lemma> existLemmas = findExistLemmas(usesUrls, lemmas);

        Map<Page,List<Index>> mayBeSuitablePages = findSuitablePages(existLemmas);
        return mayBeSuitablePages.isEmpty() ? Collections.emptyList() : checkRelevance(mayBeSuitablePages);
    }



    private Set<String> parseToLemmas(String searchedQuery) {
        return new TextToLemmaParser().parse(searchedQuery).keySet();
    }

    private List<Site> findUsesUrls(String mayBeUrl) {
        if (mayBeUrl == null || mayBeUrl.isBlank()) {
            return siteRepository.findAll();
        } else {
            Optional<Site> mayBeSite = siteRepository.findSiteByUrl(mayBeUrl);
            if (mayBeSite.isPresent()) {
                return List.of(mayBeSite.get());
            } else {
                throw new RuntimeException("Индексация сайта ещё не закончена");
            }
        }
    }


    private List<Lemma> findExistLemmas(List<Site> usesSites, Set<String> lemmas) {
        return usesSites.stream()
                .map(site -> lemmaRepository.findAllBySiteIdAndLemmas(site, lemmas)
                        .stream()
                        .filter(lemma -> lemma.getFrequency() <= (site.getPages().size() * PERCENT_OF_TOTAL_COUNT))
                        .toList())
                .reduce(new ArrayList<>(),(allLemmas, currentList) -> {
                    allLemmas.addAll(currentList);
                    return allLemmas;
                })
                .stream()
                .sorted((Lemma l1, Lemma l2) -> l1.getFrequency().compareTo(l2.getFrequency()))
                .toList();
    }

    private Map<Page,List<Index>> findSuitablePages(List<Lemma> targetLemmas) {
        int counter = 0;
        int max = 2;
        Map<Page,List<Index>> suitablePages = new HashMap<>();
        for (Lemma targetLemma : targetLemmas) {
            if (counter == max) {
                break;
            }
            suitableLemmas.add(targetLemma.getLemma());
            if(suitablePages.isEmpty()){
                suitablePages = indexRepository.findAllByLemma(targetLemma)
                        .stream()
                        .collect(Collectors.toMap(Index::getPage, List::of));
                continue;
            }

            Map<Page,Index> currentPages = indexRepository.findAllByLemma(targetLemma)
                    .stream()
                    .collect(Collectors.toMap(Index::getPage, index -> index));

            suitablePages = getUpdatedSuitablePages(suitablePages, currentPages);

            counter++;
        }

        return suitablePages;
    }

    private static Map<Page, List<Index>> getUpdatedSuitablePages(Map<Page, List<Index>> suitablePages, Map<Page, Index> currentPages) {
        Map<Page,List<Index>> updatedSuitablePages =  new HashMap<>();
        for(Map.Entry<Page,List<Index>> entry : suitablePages.entrySet()){
            Page suitablePage = entry.getKey();
            for(Map.Entry<Page,Index> currentEntry : currentPages.entrySet()){
                Page currentPage = currentEntry.getKey();
                if(suitablePage.equals(currentPage)){
                    List<Index> currentSuitableIndexes = entry.getValue();
                    List<Index> updateSuitableIndexes = new ArrayList<>(currentSuitableIndexes);
                    Index currentIndex = currentEntry.getValue();
                    updateSuitableIndexes.add(currentIndex);
                    updatedSuitablePages.put(suitablePage,updateSuitableIndexes);
                }
            }
        }
        return updatedSuitablePages;
    }

    private List<ShowPageDto> checkRelevance(Map<Page,List<Index>> suitablePages) {
        float maxRelevance = Integer.MIN_VALUE;
        Map<Float,ShowPageDto> pageAndRelevance = new HashMap<>();
        for(Map.Entry<Page,List<Index>> entry : suitablePages.entrySet()){
            Page currentPage = entry.getKey();
            List<Index> currentIndexes = entry.getValue();
            Float maxRankByCurrentPage = currentIndexes.stream()
                    .map(Index::getRank)
                    .reduce(Float::sum).get();

            ShowPageDto showPageDto = createShowPageDto(currentPage);
            maxRelevance = Math.max(maxRankByCurrentPage,maxRelevance);
            pageAndRelevance.put(maxRankByCurrentPage,showPageDto);
        }

        Map<Float,ShowPageDto> showPageByRelevance = new TreeMap<>();
        for(Map.Entry<Float,ShowPageDto> entry : pageAndRelevance.entrySet()){
            Float currentRelevance = entry.getKey();
            Float relativeRelevance = currentRelevance / maxRelevance;
            ShowPageDto currentPage = entry.getValue();
            showPageByRelevance.put(relativeRelevance,currentPage);
        }

        List<ShowPageDto> showList = new ArrayList<>();
        for(Map.Entry<Float,ShowPageDto> entry : showPageByRelevance.entrySet()){
            showList.add(entry.getValue());
        }


        return showList;
    }

    private ShowPageDto createShowPageDto(Page page){
        String pathToPage = page.getPath();
        String content = page.getContent();
        SnippetCreator snippetCreatorTask = new SnippetCreatorImpl(suitableLemmas, LuceneMorphologyGiver.getRussian());
        String snippet = snippetCreatorTask.createSnippet(content);
        return new ShowPageDto(pathToPage,"random title", snippet);
    }
}
