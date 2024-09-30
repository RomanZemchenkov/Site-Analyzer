package searchengine.services.searcher.analyzer.site_analyzer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import searchengine.services.dto.site.ShowSiteDto;

@EqualsAndHashCode(of = "siteDto")
@Getter
public class ParseContext {
    private final ShowSiteDto siteDto;
    private final SiteAnalyzerTaskFactory factory;
    @Setter
    private volatile boolean ifErrorResponse = false;

    public ParseContext(ShowSiteDto siteDto, SiteAnalyzerTaskFactory factory) {
        this.siteDto = siteDto;
        this.factory = factory;
    }
}
