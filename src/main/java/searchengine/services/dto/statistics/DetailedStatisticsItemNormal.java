package searchengine.services.dto.statistics;

import lombok.Getter;

@Getter
public class DetailedStatisticsItemNormal extends DetailedStatisticsItem{

    public DetailedStatisticsItemNormal(String url, String name, String status, String statusTime, long pages, long lemmas) {
        super(url, name, status, statusTime, pages, lemmas);
    }
}
