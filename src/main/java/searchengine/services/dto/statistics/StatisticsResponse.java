package searchengine.services.dto.statistics;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StatisticsResponse {

    private final boolean result;
    private final StatisticsData statistics;

    public StatisticsResponse(boolean result, StatisticsData statistics) {
        this.result = result;
        this.statistics = statistics;
    }
}
