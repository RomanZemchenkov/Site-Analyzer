package searchengine.services.dto.page;

import lombok.Getter;

@Getter
public class CreatePageWithMainSiteUrlDto {

    private final String mainUrl;
    private final String path;
    private final String code;
    private final String content;

    public CreatePageWithMainSiteUrlDto(String mainUrl, String path, String code, String content) {
        this.mainUrl = mainUrl;
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
