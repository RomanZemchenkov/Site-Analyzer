package searchengine.dao.repository.index;

import searchengine.dao.model.Index;

import java.util.List;

public interface CustomIndexRepository {

    void createBatch(List<Index> indexList);

}
