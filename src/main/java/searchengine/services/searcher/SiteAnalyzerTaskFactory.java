package searchengine.services.searcher;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.dao.repository.PageRepository;
import searchengine.services.event_listeners.publisher.EventPublisher;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
@RequiredArgsConstructor
public class SiteAnalyzerTaskFactory {

    private final EventPublisher eventPublisher;
    private final PageRepository repository;
    private final Set<String> urls = new ConcurrentSkipListSet<>();

    public SiteAnalyzerTask createTask(String url, ParseContext context){
        return new SiteAnalyzerTask(url, context, eventPublisher, repository, urls);
    }
}
