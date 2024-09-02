package searchengine.services.dto.site;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CreateSiteDto {

    private final String url;
    private final String name;

    public CreateSiteDto(String url, String name) {
        this.url = url;
        this.name = name;
    }
}
