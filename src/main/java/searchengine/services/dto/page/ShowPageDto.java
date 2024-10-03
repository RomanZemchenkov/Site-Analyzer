package searchengine.services.dto.page;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ShowPageDto {

    private final String uri;
    private final String title;
    private final String snippet;
    private final String relevance;
    private final String siteName;
    private final String site;

    public ShowPageDto(String uri, String title, String snippet, String relevance, String siteName, String site) {
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
        this.siteName = siteName;
        this.site = site;
    }
}
