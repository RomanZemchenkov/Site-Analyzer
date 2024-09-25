package searchengine.dao.repository.statistic;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;
import searchengine.dao.model.Statistic;

public interface CustomStatisticRepository {

    Statistic readStatisticBySiteId(Site site);

    void writeStatistics();

    Tuple readDetailInformation(Site site);


    @Modifying
    @Transactional
    void deleteAllBySite(Site site);

}
