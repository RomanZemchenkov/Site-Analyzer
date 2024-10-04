package searchengine.dao.repository.index;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;

import java.util.List;

@Transactional(readOnly = true)
public interface CustomIndexRepository {

    void batchSave(List<Index> indexList);

    @Modifying
    @Transactional
    void deleteAllByPage(Page page);

    List<Index> findAllIndexesWithPageByLemmas(Lemma lemma);

}
