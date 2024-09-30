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

    public void createLemma(Page page, Site site) {
        TextToLemmaParserImpl parser = new TextToLemmaParserImpl();
        String htmlContent = page.getContent();
        Map<String, Integer> mapOfLemmas = parser.parse(htmlContent);

        HashMap<Lemma, Integer> lemmaAndCountsOfOneSite = addLemmas(mapOfLemmas, site);
        GlobalVariables.PAGE_AND_LEMMAS_WITH_COUNT.put(page, lemmaAndCountsOfOneSite);
        GlobalVariables.COUNT_OF_LEMMAS.set(lemmasAndCounts.size());
    }

    private HashMap<Lemma, Integer> addLemmas(Map<String, Integer> mapOfLemmas, Site site) {
        HashMap<Lemma,Integer> lemmasAndCountsOfOneSite = new HashMap<>();
        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
            String lemma = entry.getKey();
            int countOfLemma = entry.getValue();
            Lemma saveLemma = new Lemma(lemma, site);

            for (Map.Entry<Lemma,Integer> existLemmas : lemmasAndCounts.entrySet()){
                Lemma savedLemma = existLemmas.getKey();
                if (savedLemma.getLemma().equals(lemma)){
                    saveLemma = savedLemma;
                    break;
                }
            }
            lemmasAndCounts.put(saveLemma, lemmasAndCounts.getOrDefault(saveLemma, 0) + 1);
            lemmasAndCountsOfOneSite.put(saveLemma,countOfLemma);
        }
        return lemmasAndCountsOfOneSite;
    }

 //    private HashMap<Lemma, Integer> addLemmas(HashMap<String, Integer> mapOfLemmas, Site site) {
//        HashMap<Lemma,Integer> lemmasAndCount = new HashMap<>();
//        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
//            String lemma = entry.getKey();
//            int countOfLemma = entry.getValue();
//            Lemma saveLemma = new Lemma(lemma, site);
//
//            Lemma existingLemma = lemmasAndCounts.keySet().stream()
//                    .filter(lem -> lem.getLemma().equals(lemma))
//                    .findFirst()
//                    .orElseGet(() -> new Lemma(lemma,site));
//
//            lemmasAndCounts.merge(existingLemma, 1 , Integer::sum);
//            lemmasAndCount.put(saveLemma,countOfLemma);
//        }
//        return lemmasAndCount;
//    }

}
