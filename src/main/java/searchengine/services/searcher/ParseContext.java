package searchengine.services.searcher;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

@Getter
public class ParseContext {

    private final String siteId;
    private final String siteName;
    private final String mainUrl;
    private final SiteAnalyzerTaskFactory factory;

    public ParseContext(String siteId, String siteName, String mainUrl, SiteAnalyzerTaskFactory factory) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.mainUrl = mainUrl;
        this.factory = factory;
    }
}
