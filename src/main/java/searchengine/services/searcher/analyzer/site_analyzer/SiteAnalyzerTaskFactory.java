package searchengine.services.searcher.analyzer.site_analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTaskFactory;

import java.util.concurrent.ConcurrentSkipListSet;


@Component
@RequiredArgsConstructor
public class SiteAnalyzerTaskFactory {

    private final EventPublisher eventPublisher;
    private final PageAnalyzerTaskFactory pageAnalyzerTaskFactory;

    public SiteAnalyzerTask createTask(String url, ParseContext context, ConcurrentSkipListSet<String> useUrlsSet){
        return new SiteAnalyzerTask(url, context, eventPublisher, useUrlsSet, pageAnalyzerTaskFactory);
    }
}
