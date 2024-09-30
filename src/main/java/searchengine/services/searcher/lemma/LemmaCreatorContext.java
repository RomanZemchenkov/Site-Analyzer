package searchengine.services.searcher.lemma;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@EqualsAndHashCode
public class LemmaCreatorContext {

    private final Site site;
    private final ConcurrentLinkedDeque<Page> pageList;
    private final LemmaCreatorTaskFactory creatorTaskFactory;
    private final ConcurrentHashMap<Lemma, Integer> countOfLemmas;

    public LemmaCreatorContext(Site site, ConcurrentLinkedDeque<Page> pageList, LemmaCreatorTaskFactory creatorTaskFactory, ConcurrentHashMap<Lemma, Integer> countOfLemmas) {
        this.site = site;
        this.pageList = pageList;
        this.creatorTaskFactory = creatorTaskFactory;
        this.countOfLemmas = countOfLemmas;
    }
}
