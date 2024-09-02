package searchengine.services.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import searchengine.dao.model.Site;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.dto.site.UpdateSiteDto;

@Mapper(componentModel = "spring")
public interface SiteMapper {

    @Mapping(target = "url", source = "url")
    @Mapping(target = "name", source = "name")
    Site mapToSite(CreateSiteDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "statusTime", source = "statusTime")
    Site mapToSite(UpdateSiteDto dto, @MappingTarget Site existSite);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "name", source = "name")
    ShowSiteDto mapToShow(Site site);
}
