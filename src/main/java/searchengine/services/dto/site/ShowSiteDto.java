package searchengine.services.dto.site;

import lombok.Getter;

@Getter
public class ShowSiteDto {

    private final String id;
    private final String url;
    private final String name;

    public ShowSiteDto(String id, String url, String name) {
        this.id = id;
        this.url = url;
        this.name = name;
    }
}
