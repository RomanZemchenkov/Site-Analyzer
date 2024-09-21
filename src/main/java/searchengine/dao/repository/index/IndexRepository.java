package searchengine.dao.repository.index;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Integer>, CustomIndexRepository {

    List<Index> findAllByLemma(Lemma lemma);
}
