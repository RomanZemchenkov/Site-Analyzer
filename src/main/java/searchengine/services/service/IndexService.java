package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Index;
import searchengine.dao.repository.index.IndexRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final IndexRepository indexRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createIndex(List<Index> indexesBySite){
        indexRepository.batchSave(indexesBySite);
    }
}
