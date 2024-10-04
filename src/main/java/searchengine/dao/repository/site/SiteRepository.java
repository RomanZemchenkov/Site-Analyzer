package searchengine.dao.repository.site;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Optional;

/*
Подумать о создании запроса на обновление только статуса и времени
 */
@Transactional(readOnly = true)
public interface SiteRepository extends JpaRepository<Site, Integer>, CustomSiteRepository {

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "Site.withAllPages")
    Optional<Site> findSiteByName(String name);

    Optional<Site> findSiteByUrl(String url);

//    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, value = "Site.withAllPages")
//    @NotNull
//    List<Site> findAll();


}
