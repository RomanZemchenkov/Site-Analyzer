package searchengine.dao.repository.site;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Optional;

/*
Подумать о создании запроса на обновление только статуса и времени
 */

@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public interface SiteRepository extends JpaRepository<Site, Integer>, CustomSiteRepository {

    Optional<Site> findSiteByName(String name);

    Optional<Site> findSiteByUrl(String url);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "Site.withAllPages")
    List<Site> findAll();

}
