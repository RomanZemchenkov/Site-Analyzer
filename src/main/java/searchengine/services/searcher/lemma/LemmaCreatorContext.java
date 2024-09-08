package searchengine.services.searcher.lemma;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@EqualsAndHashCode
public class LemmaCreatorContext {

    private final Site site;
    private final ConcurrentLinkedDeque<Page> pageList;
    private final LemmaCreatorTaskFactory creatorTaskFactory;

    public LemmaCreatorContext(Site site, ConcurrentLinkedDeque<Page> pageList, LemmaCreatorTaskFactory creatorTaskFactory) {
        this.site = site;
        this.pageList = pageList;
        this.creatorTaskFactory = creatorTaskFactory;
    }
}
