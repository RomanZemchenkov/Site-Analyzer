package searchengine.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;

import java.util.Optional;

/*
Подумать о создании запроса на обновление только статуса и времени
 */

@Transactional(readOnly = true)
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Optional<Site> findSiteByName(String name);

    Site findSiteByUrl(String url);

}
