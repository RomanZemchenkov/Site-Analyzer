package searchengine.services.searcher.lemma;

import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.services.parser.TextToLemmaParser;

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

        addLemmas(mapOfLemmas, site);
        System.out.println("Лемма создана");
    }

    private void addLemmas(HashMap<String, Integer> mapOfLemmas, Site site) {
        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
            String lemma = entry.getKey();
            Lemma saveLemma = new Lemma(lemma, site);
            lemmasAndCounts.put(saveLemma, lemmasAndCounts.getOrDefault(saveLemma, 0) + 1);
        }
    }
}
