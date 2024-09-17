package searchengine.dao.repository.site;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Set;

public interface CustomSiteRepository{

    List<Site> findAllByName(Set<String> names);

    @Modifying
    @Transactional
    void deleteAllInfoBySite(Site site);
}
