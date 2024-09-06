package searchengine.services.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.page.CreatePageWithMainSiteUrlDto;

@Mapper(componentModel = "spring")
public interface PageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "dto.code")
    @Mapping(target = "content", source = "dto.content")
    @Mapping(target = "site", source = "site")
    Page mapToPage(CreatePageDto dto, Site site);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "dto.code")
    @Mapping(target = "content", source = "dto.content")
    @Mapping(target = "site", source = "site")
    Page mapToPage(CreatePageWithMainSiteUrlDto dto, Site site);
}
