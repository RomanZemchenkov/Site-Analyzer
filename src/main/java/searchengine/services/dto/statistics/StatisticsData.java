package searchengine.services.dto.statistics;

import lombok.Getter;

import java.util.List;

@Getter
public class StatisticsData {
    private final TotalStatistics total;
    private final List<DetailedStatisticsItem> detailed;

    public StatisticsData(TotalStatistics total, List<DetailedStatisticsItem> detailed) {
        this.total = total;
        this.detailed = detailed;
    }
}
