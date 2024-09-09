package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Index;
import searchengine.dao.repository.IndexRepository;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final IndexRepository indexRepository;

    public void createIndex(){
        Index index = new Index();
        indexRepository.save(index);
    }
}
