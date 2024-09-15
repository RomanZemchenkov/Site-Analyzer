package searchengine.dao.repository.statistic;

import jakarta.persistence.Tuple;
import searchengine.dao.model.Site;
import searchengine.dao.model.Statistic;

public interface CustomStatisticRepository {

    Statistic readStatisticBySiteId(Site site);

    void writeStatistics();

    Tuple readDetailInformation(Site site);

}
