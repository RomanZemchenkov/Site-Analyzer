package searchengine.services.mapper;

import jakarta.persistence.Tuple;
import org.mapstruct.Mapper;
import searchengine.dao.model.Status;
import searchengine.services.dto.statistics.DetailedStatisticsItem;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface StatisticMapper {
    default DetailedStatisticsItem mapToStatisticItem(Tuple tuple) {
        String url = tuple.get(0, String.class);
        String name = tuple.get(1, String.class);
        String statusByString = tuple.get(2, Status.class).toString();
        String lastError = tuple.get(3, String.class);
        OffsetDateTime statusTime = tuple.get(4, OffsetDateTime.class);
        Long countOfPages = tuple.get(5, Long.class);
        if (countOfPages == null) {
            countOfPages = 0L;
        }
        Long countOfLemmas = tuple.get(6, Long.class);
        if (countOfLemmas == null) {
            countOfLemmas = 0L;
        }

        DetailedStatisticsItem statisticsItem;
        long statusTimeInstant = statusTime.toInstant().toEpochMilli();
        statisticsItem = new DetailedStatisticsItem(url, name, statusByString, statusTimeInstant,lastError, countOfPages, countOfLemmas);
        return statisticsItem;
    }
}
