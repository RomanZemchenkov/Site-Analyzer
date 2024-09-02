package searchengine.services.event_listeners.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.event.AnalyzedPageEvent;
import searchengine.services.event_listeners.event.CreatePageEvent;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishEvent(AnalyzedPageEvent event){
        publisher.publishEvent(event);
    }

    public void publicUpdateSiteEvent(CreatePageEvent event){
        publisher.publishEvent(event);
    }
}
