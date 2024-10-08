package searchengine.services.searcher.analyzer.page_analyzer;

import lombok.Getter;
import lombok.Setter;
import searchengine.dao.model.Site;
import searchengine.services.dto.site.ShowSiteDto;


@Getter
public class PageParseContext {

    private final Site site;
    @Setter
    @Getter
    private static volatile boolean ifStop = false;

    public PageParseContext(Site site) {
        this.site = site;
    }
}
