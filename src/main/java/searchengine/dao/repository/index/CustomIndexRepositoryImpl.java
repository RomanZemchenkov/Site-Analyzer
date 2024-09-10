package searchengine.dao.repository.index;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import searchengine.dao.model.Index;

import java.util.List;

@RequiredArgsConstructor
public class CustomIndexRepositoryImpl implements CustomIndexRepository{

    private final EntityManager entityManager;

    @Override
    public void createBatch(List<Index> indexList) {
        int batchSize = 1000;
        int counter = 0;
        for(Index index : indexList){
            entityManager.persist(index);
            counter++;
            if(counter == batchSize){
                entityManager.flush();
                entityManager.clear();
                counter = 0;
            }
        }
        entityManager.flush();
        entityManager.clear();
        System.out.println("Сохранение индексов произошло");
    }
}
