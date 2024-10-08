package searchengine.services.searcher.analyzer.site_analyzer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.concurrent.CopyOnWriteArraySet;

@EqualsAndHashCode(of = "site")
@Getter
public class ParseContext {
    private final Site site;
    private final SiteAnalyzerTaskFactory factory;
    @Setter
    private volatile boolean ifErrorResponse = false;
    @Setter
    private String errorContent;
    private final CopyOnWriteArraySet<Page> pagesSet = new CopyOnWriteArraySet<>();

    public ParseContext(Site site, SiteAnalyzerTaskFactory factory) {
        this.site = site;
        this.factory = factory;
    }
}
