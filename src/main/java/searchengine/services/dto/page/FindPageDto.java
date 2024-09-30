package searchengine.services.dto.page;

import lombok.Getter;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

@Getter
public class FindPageDto {

    private final Page savedPage;
    private final Site site;

    public FindPageDto(Page savedPage, Site site) {
        this.savedPage = savedPage;
        this.site = site;
    }
}
