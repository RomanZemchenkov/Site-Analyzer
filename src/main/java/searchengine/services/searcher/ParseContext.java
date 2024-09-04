package searchengine.services.searcher;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = "siteName")
@Getter
public class ParseContext {

    private final String siteId;
    private final String siteName;
    private final String mainUrl;
    private final SiteAnalyzerTaskFactory factory;
    @Setter
    private volatile boolean indexingStopFlag = false;

    public ParseContext(String siteId, String siteName, String mainUrl, SiteAnalyzerTaskFactory factory) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.mainUrl = mainUrl;
        this.factory = factory;
    }
}
