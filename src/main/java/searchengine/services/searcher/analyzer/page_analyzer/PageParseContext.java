package searchengine.services.searcher.analyzer.page_analyzer;

import lombok.Getter;
import lombok.Setter;
import searchengine.services.dto.site.ShowSiteDto;


@Getter
public class PageParseContext {

    private final ShowSiteDto site;
    @Setter
    @Getter
    private static volatile boolean ifStop = false;

    public PageParseContext(ShowSiteDto site) {
        this.site = site;
    }
}
