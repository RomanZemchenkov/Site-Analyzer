package searchengine.services.searcher.lemma;

import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;

import java.util.Map;

@Getter
public class CreateLemmaResult {

    private final Page page;
    private final Map<Lemma,Integer> countOfLemmasByPage;

    public CreateLemmaResult(Page page, Map<Lemma, Integer> countOfLemmasByPage) {
        this.page = page;
        this.countOfLemmasByPage = countOfLemmasByPage;
    }
}