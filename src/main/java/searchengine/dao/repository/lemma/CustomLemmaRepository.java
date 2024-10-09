package searchengine.dao.repository.lemma;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Set;

public interface CustomLemmaRepository {

    List<Lemma> batchSave(List<Lemma> lemmaList);

    List<Lemma> findAllBySiteIdAndLemmasByMaxFrequency(Site site, Set<String> lemmas,float maxFrequency);

    void checkExistAndSaveOrUpdate(List<Lemma> lemmaList, Site site);

    @Modifying
    @Transactional
    void deleteAllBySite(Site site);
}
