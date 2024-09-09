package searchengine.dao.repository.lemma;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Lemma;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomLemmaRepositoryImpl implements CustomLemmaRepository{

    private final EntityManager entityManager;

    @Override
    public List<Lemma> batchSave(List<Lemma> lemmaList) {
        int batchSize = 1000;
        int counter = 0;
        for(Lemma lemma : lemmaList){
            entityManager.persist(lemma);
            counter++;
            if(counter == batchSize){
                entityManager.flush();
                entityManager.clear();
                counter = 0;
            }
        }
        entityManager.flush();
        entityManager.clear();
        return lemmaList;
    }
}
