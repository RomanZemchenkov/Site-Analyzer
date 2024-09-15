package searchengine.services.dto.statistics;

import lombok.Getter;

@Getter
public class DetailedStatisticsItemError extends DetailedStatisticsItem{

    private final String error;

    public DetailedStatisticsItemError(String url, String name, String status, String statusTime, long pages, long lemmas, String error) {
        super(url, name, status, statusTime, pages, lemmas);
        this.error = error;
    }
}
