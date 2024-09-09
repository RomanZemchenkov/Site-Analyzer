package searchengine.services.searcher.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.event_listeners.publisher.EventPublisher;

@Component
@RequiredArgsConstructor
public class LemmaCreatorTaskFactory {

    private final EventPublisher publisher;

    public LemmaCreatorTask createTask(LemmaCreatorContext context){
        return new LemmaCreatorTask(context, publisher);
    }
}
