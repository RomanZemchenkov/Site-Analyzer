package searchengine.services.dto.site;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
public class UpdateSiteDto {

    private final String id;
    private final String status;
    private final OffsetDateTime statusTime;
    private String lastError;
    private final String url;
    private final String name;

    public UpdateSiteDto(String id, String status, String lastError, String url, String name) {
        this.id = id;
        this.status = status;
        this.lastError = lastError;
        this.statusTime = OffsetDateTime.now(ZoneId.systemDefault());
        this.url = url;
        this.name = name;
    }

    public UpdateSiteDto(String id, String status, String url, String name) {
        this.id = id;
        this.status = status;
        this.statusTime = OffsetDateTime.now(ZoneId.systemDefault());
        this.url = url;
        this.name = name;
    }
}
