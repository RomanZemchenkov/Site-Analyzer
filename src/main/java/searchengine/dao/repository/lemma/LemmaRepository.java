package searchengine.dao.repository.lemma;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.dao.model.Lemma;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer>, CustomLemmaRepository {

    List<Lemma> findAllBySiteId(Integer siteId);

}
