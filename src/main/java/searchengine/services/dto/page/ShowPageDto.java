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
    @Setter
    private String relevance;
    @Setter
    private String siteName;
    @Setter
    private String site;

    public ShowPageDto(String uri, String title, String snippet) {
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
    }
}
