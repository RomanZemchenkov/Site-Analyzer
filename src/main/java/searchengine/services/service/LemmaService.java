package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Lemma;
import searchengine.dao.repository.lemma.LemmaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional()
public class LemmaService {

    private final LemmaRepository lemmaRepository;

    public List<Lemma> createBatch(List<Lemma> lemmaList){
        return lemmaRepository.batchSave(lemmaList);
    }

}
