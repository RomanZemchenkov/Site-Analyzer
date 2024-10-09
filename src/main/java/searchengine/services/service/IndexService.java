package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.repository.index.IndexRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final IndexRepository indexRepository;

    public void saveBatchIndexes(Page page, List<Lemma> lemmasForPage, Map<Lemma, Integer> lemmasAndCounts) {
        List<Index> list = lemmasForPage
                .stream()
                .map(lemma -> new Index(page, lemma, (float) lemmasAndCounts.get(lemma)))
                .toList();
        indexRepository.batchSave(list);
    }

}
