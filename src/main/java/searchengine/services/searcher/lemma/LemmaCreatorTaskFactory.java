package searchengine.services.searcher.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class LemmaCreatorTaskFactory {


    public LemmaCreatorTask createTask(LemmaCreatorContext context, ConcurrentHashMap<Page, Map<Lemma,Integer>> countOfLemmasByPage){
        return new LemmaCreatorTask(context, countOfLemmasByPage );
    }
}
