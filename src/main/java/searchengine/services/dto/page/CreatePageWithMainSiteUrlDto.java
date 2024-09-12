package searchengine.services.dto.page;

import lombok.Getter;

@Getter
public class CreatePageWithMainSiteUrlDto {

    private final String siteUrl;
    private final String siteName;
    private final String pageUrl;
    private final String code;
    private final String content;

    public CreatePageWithMainSiteUrlDto(String siteUrl, String siteName, String pageUrl, String code, String content) {
        this.siteUrl = siteUrl;
        this.siteName = siteName;
        this.pageUrl = pageUrl;
        this.code = code;
        this.content = content;
    }
}
