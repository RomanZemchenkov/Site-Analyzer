package searchengine.dao.repository.index;

import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;

import java.util.List;

public interface CustomIndexRepository {

    void createBatch(List<Index> indexList);

    List<Integer> getAllPagesId(List<Index> indexList);

}
