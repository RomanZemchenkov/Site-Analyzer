package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.repository.index.IndexRepository;
import searchengine.dao.repository.lemma.LemmaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;

    @Transactional()
    public void createIndex(List<Index> indexesBySite){
        System.out.println("Начало сохранения индексов");
        indexRepository.batchSave(parseToIndex(indexesBySite));
    }

    private List<Index> parseToIndex(List<Index> indexList){
        for(Index index : indexList){
            Lemma indexLemma = index.getLemma();
            if(indexLemma.getId() == null){
                indexLemma = lemmaRepository.findLemmaByLemmaAndSite(indexLemma.getLemma(), indexLemma.getSite());
                index.setLemma(indexLemma);
            }
        }
        return indexList;
    }
}
