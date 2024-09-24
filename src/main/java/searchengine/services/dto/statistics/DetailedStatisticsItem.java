package searchengine.services.dto.statistics;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DetailedStatisticsItem {
    private final String url;
    private final String name;
    private final String status;
    private final long statusTime;
    private final String error;
    private final long pages;
    private final long lemmas;

    public DetailedStatisticsItem(String url, String name, String status, long statusTime, String error, long pages, long lemmas) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.pages = pages;
        this.lemmas = lemmas;
    }
}
