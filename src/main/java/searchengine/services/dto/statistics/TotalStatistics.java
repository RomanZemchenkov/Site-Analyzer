package searchengine.services.dto.statistics;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TotalStatistics {
    private final long sites;
    private final long pages;
    private final long lemmas;
    private final boolean indexing;

    public TotalStatistics(long sites, long pages, long lemmas, boolean indexing) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.indexing = indexing;
    }
}
