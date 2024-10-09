package searchengine.services.searcher.lemma;

import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.services.parser.lemma.TextToLemmaParserImpl;
import searchengine.services.GlobalVariables;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class LemmaWriter {

    private final ConcurrentHashMap<Lemma, Integer> lemmasAndCounts;

    public LemmaWriter(ConcurrentHashMap<Lemma, Integer> lemmasAndCounts) {
        this.lemmasAndCounts = lemmasAndCounts;
    }

    public CreateLemmaResult createLemma(Page page, Site site) {
        TextToLemmaParserImpl parser = new TextToLemmaParserImpl();
        String htmlContent = page.getContent();
        Map<String, Integer> mapOfLemmas = parser.parse(htmlContent);

        GlobalVariables.COUNT_OF_LEMMAS.set(lemmasAndCounts.size());
        return new CreateLemmaResult(page, addLemmas(mapOfLemmas, site));
    }

    private Map<Lemma, Integer> addLemmas(Map<String, Integer> mapOfLemmas, Site site) {
        Map<Lemma, Integer> countOfLemmasByPage = new HashMap<>();
        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
            String lemma = entry.getKey();
            Integer countByPage = entry.getValue();
            Lemma saveLemma = new Lemma(lemma, site);
            for (Map.Entry<Lemma, Integer> existLemmas : lemmasAndCounts.entrySet()) {
                Lemma savedLemma = existLemmas.getKey();
                if (savedLemma.getLemma().equals(lemma)) {
                    saveLemma = savedLemma;
                    break;
                }
            }
            countOfLemmasByPage.put(saveLemma, countByPage);
            lemmasAndCounts.put(saveLemma, lemmasAndCounts.getOrDefault(saveLemma, 0) + 1);
        }
        return countOfLemmasByPage;
    }

}