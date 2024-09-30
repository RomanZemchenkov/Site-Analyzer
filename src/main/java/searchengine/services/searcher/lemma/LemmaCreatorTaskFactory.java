package searchengine.services.searcher.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LemmaCreatorTaskFactory {

    public LemmaCreatorTask createTask(LemmaCreatorContext context){
        return new LemmaCreatorTask(context);
    }
}
