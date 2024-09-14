package searchengine.services.event_listeners.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.event.IndexCreateEvent;
import searchengine.services.event_listeners.event.CreatePageEvent;
import searchengine.services.event_listeners.event.UpdateSiteEvent;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishCreatePageEvent(CreatePageEvent event){
        publisher.publishEvent(event);
    }

    public void publishUpdateSiteEvent(UpdateSiteEvent event){
        publisher.publishEvent(event);
    }

    public void publishIndexCreateEvent(IndexCreateEvent event){
        publisher.publishEvent(event);
    }
}
