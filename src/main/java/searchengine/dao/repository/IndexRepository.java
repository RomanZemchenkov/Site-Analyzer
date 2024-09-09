package searchengine.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.dao.model.Index;

public interface IndexRepository extends JpaRepository<Index, Integer> {
}
