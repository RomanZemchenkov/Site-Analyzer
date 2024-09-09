package searchengine.services.event_listeners;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import searchengine.dao.model.Page;

import java.util.HashMap;

@Getter
@ToString
public class IndexCreateEvent extends ApplicationEvent {

    private final Page page;
    private final HashMap<String, Integer> countOfLemmas;

    public IndexCreateEvent(Page page, HashMap<String, Integer> countOfLemmas) {
        super(page);
        this.page = page;
        this.countOfLemmas = countOfLemmas;
    }
}
