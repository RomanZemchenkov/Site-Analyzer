package searchengine.dao.repository.site;

import searchengine.dao.model.Site;

import java.util.List;
import java.util.Set;

public interface CustomSiteRepository{

    List<Site> findAllByName(Set<String> names);
}
