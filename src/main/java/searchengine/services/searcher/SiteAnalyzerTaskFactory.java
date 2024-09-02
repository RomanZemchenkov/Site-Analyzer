package searchengine.services.searcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dao.repository.RedisRepository;
import searchengine.services.event_listeners.publisher.EventPublisher;

import java.util.concurrent.ForkJoinPool;


@Component
@RequiredArgsConstructor
public class SiteAnalyzerTaskFactory {

    private final EventPublisher eventPublisher;
    private final RedisRepository redisRepository;

    public SiteAnalyzerTask createTask(String url, ParseContext context){
        return new SiteAnalyzerTask(url, context, eventPublisher, redisRepository);
    }
}
