package searchengine.services.dto.page;

import lombok.Getter;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

@Getter
public class CreatedPageInfoDto {

    private final Page savedPage;
    private final Site site;

    public CreatedPageInfoDto(Page savedPage, Site site) {
        this.savedPage = savedPage;
        this.site = site;
    }
}
