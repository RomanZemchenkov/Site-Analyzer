package searchengine.services.dto.page;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ShowPageDto {

    private final String pathToPage;
    private final String pageTitle;
    private final String snippet;
    @Setter
    private String relevance;

    public ShowPageDto(String pathToPage, String pageTitle, String snippet) {
        this.pathToPage = pathToPage;
        this.pageTitle = pageTitle;
        this.snippet = snippet;
    }
}
