package searchengine.services.event_listeners.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FinishOrStopIndexingEvent extends ApplicationEvent {

    private final String siteUrl;

    public FinishOrStopIndexingEvent(String  siteUrl) {
        super(siteUrl);
        this.siteUrl = siteUrl;
    }
}
