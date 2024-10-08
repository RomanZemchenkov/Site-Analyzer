package searchengine.dao.repository.lemma;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer>, CustomLemmaRepository {

    List<Lemma> findAllBySiteId(Integer siteId);

    @Transactional(readOnly = true)
    Lemma findLemmaByLemmaAndSite(String lemma, Site site);

}
