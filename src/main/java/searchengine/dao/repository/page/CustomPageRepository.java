package searchengine.dao.repository.page;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;

public interface CustomPageRepository{

    @Modifying
    @Transactional
    void deleteAllBySite(Site site);
}
