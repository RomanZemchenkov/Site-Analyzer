package searchengine.services.searcher.analyzer.page_analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.publisher.EventPublisher;


@Component
@RequiredArgsConstructor
public class PageAnalyzerTaskFactory {

    private final EventPublisher publisher;

    public PageAnalyzerTask createTask(String pageUrl, PageParseContext context) {
        return new PageAnalyzerTaskImpl(pageUrl, context, publisher);
    }
}