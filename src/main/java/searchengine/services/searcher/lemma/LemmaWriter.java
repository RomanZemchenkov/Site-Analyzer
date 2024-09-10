package searchengine.services.searcher.lemma;

import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.services.parser.TextToLemmaParser;
import searchengine.services.searcher.GlobalVariables;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class LemmaWriter {

    private final ConcurrentHashMap<Lemma, Integer> lemmasAndCounts;

    public LemmaWriter(ConcurrentHashMap<Lemma, Integer> lemmasAndCounts) {
        this.lemmasAndCounts = lemmasAndCounts;
    }

    public void createLemma(Page page, Site site) {
        TextToLemmaParser parser = new TextToLemmaParser();
        String htmlContent = page.getContent();
        HashMap<String, Integer> mapOfLemmas = parser.parse(htmlContent);

        HashMap<Lemma, Integer> lemmaIntegerHashMap = addLemmas(mapOfLemmas, site);
        GlobalVariables.pageAndLemmasWithCount.put(page,lemmaIntegerHashMap);
    }

    private HashMap<Lemma, Integer> addLemmas(HashMap<String, Integer> mapOfLemmas, Site site) {
        HashMap<Lemma,Integer> lemmasAndCount = new HashMap<>();
        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
            String lemma = entry.getKey();
            Lemma saveLemma = new Lemma(lemma, site);

            for (Map.Entry<Lemma,Integer> existLemmas : lemmasAndCounts.entrySet()){
                Lemma savedLemma = existLemmas.getKey();
                if (savedLemma.getLemma().equals(lemma)){
                    saveLemma = savedLemma;
                    break;
                }
            }
            lemmasAndCounts.put(saveLemma, lemmasAndCounts.getOrDefault(saveLemma, 0) + 1);
            lemmasAndCount.put(saveLemma,entry.getValue());
        }
        return lemmasAndCount;
    }
}
