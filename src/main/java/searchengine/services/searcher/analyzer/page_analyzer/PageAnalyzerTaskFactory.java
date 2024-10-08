package searchengine.services.searcher.analyzer.page_analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.service.PageService;
import searchengine.services.service.SiteService;


@Component
@RequiredArgsConstructor
public class PageAnalyzerTaskFactory {

    private final SiteService siteService;
    private final PageService pageService;

    public PageAnalyzerTask createTask(String pageUrl, PageParseContext context) {
        return new PageAnalyzerTaskImpl(pageUrl, context, siteService, pageService);
    }
}
