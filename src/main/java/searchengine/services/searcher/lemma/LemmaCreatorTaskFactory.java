package searchengine.services.searcher.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dao.repository.RedisRepository;

@Component
@RequiredArgsConstructor
public class LemmaCreatorTaskFactory {

    private final RedisRepository redisRepository;
    private final LemmaService lemmaService;

    public LemmaCreatorTask createTask(LemmaCreatorContext context){
        return new LemmaCreatorTask(redisRepository,context,lemmaService);
    }
}
