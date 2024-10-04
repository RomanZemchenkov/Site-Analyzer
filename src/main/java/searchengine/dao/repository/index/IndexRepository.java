package searchengine.dao.repository.index;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;

import java.util.List;

@Transactional(readOnly = true)
public interface IndexRepository extends JpaRepository<Index, Integer>, CustomIndexRepository {

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD,attributePaths = {"page"})
    List<Index> findAllByLemma(Lemma lemma);
}
