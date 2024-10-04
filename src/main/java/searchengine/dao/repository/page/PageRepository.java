package searchengine.dao.repository.page;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer>, CustomPageRepository {

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD,attributePaths = "site")
    Optional<Page> findByPath(String path);

    List<Page> findAllBySite(Site site);

}
