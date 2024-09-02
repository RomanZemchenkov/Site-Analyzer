package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.dao.repository.SiteRepository;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.mapper.SiteMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository repository;
    private final SiteMapper mapper;

    @Transactional
    public Site createSite(CreateSiteDto dto){
        String name = dto.getName();

        Optional<Site> mayBeSite = repository.findSiteByName(name);

        mayBeSite.ifPresent(repository::delete);

        Site site = mapper.mapToSite(dto);
        site.setStatus(Status.INDEXING);
        site.setStatusTime(OffsetDateTime.now(ZoneId.systemDefault()));

        return repository.saveAndFlush(site);
    }

    @Transactional
    public boolean updateSite(UpdateSiteDto dto){
        Integer id = Integer.valueOf(dto.getId());
        Site existSite = repository.findById(id).get();

        Site siteBeforeUpdate = mapper.mapToSite(dto, existSite);

        Site savedSite = repository.saveAndFlush(siteBeforeUpdate);
        return dto.getLastError().isEmpty();
    }
}
