package searchengine.dao.repository.statistic;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.dao.model.Statistic;

public interface StatisticRepository extends JpaRepository<Statistic, Integer>, CustomStatisticRepository {
}
