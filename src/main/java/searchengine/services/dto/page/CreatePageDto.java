package searchengine.services.dto.page;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CreatePageDto {

    private final String siteId;
    private final String path;
    private final String code;
    private final String content;

    public CreatePageDto(String siteId, String path, String code, String content) {
        this.siteId = siteId;
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
