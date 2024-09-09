package searchengine.services.searcher.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Lemma;
import searchengine.dao.repository.lemma.LemmaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LemmaService {

    private final LemmaCreatorTaskFactory factory;
    private final LemmaRepository lemmaRepository;

    public List<Lemma> createBatch(List<Lemma> lemmaList){
        return lemmaRepository.batchSave(lemmaList);
    }

}
