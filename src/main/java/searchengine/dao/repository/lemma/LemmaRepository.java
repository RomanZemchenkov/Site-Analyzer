package searchengine.dao.repository.lemma;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.dao.model.Lemma;

public interface LemmaRepository extends JpaRepository<Lemma, Integer>, CustomLemmaRepository {


}
