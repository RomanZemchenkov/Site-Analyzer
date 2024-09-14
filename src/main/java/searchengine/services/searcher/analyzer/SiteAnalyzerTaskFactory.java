package searchengine.services.searcher.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.publisher.EventPublisher;

import java.util.concurrent.ConcurrentSkipListSet;


@Component
@RequiredArgsConstructor
public class SiteAnalyzerTaskFactory {

    private final EventPublisher eventPublisher;

    public SiteAnalyzerTask createTask(String url, ParseContext context, ConcurrentSkipListSet<String> useUrlsSet){
        return new SiteAnalyzerTask(url, context, eventPublisher, useUrlsSet);
    }
}
