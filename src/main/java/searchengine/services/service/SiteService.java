package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.dto.site.CreateSiteDto;
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
    public Integer createSite(CreateSiteDto dto){
        System.out.println("Новый сайт создан или удалён начало");
        String name = dto.getName();

        Optional<Site> mayBeSite = repository.findSiteByName(name);

        mayBeSite.ifPresent(repository::deleteAllInfoBySite);

        Site site = mapper.mapToSite(dto);
        site.setStatus(Status.INDEXING);
        site.setStatusTime(OffsetDateTime.now(ZoneId.systemDefault()));

        Site savedSite = repository.saveAndFlush(site);
        System.out.println("Новый сайт создан или удалён конец");
        return savedSite.getId();
    }

    @Transactional
    public void updateSite(UpdateSiteDto dto){
        Integer id = Integer.valueOf(dto.getId());
        Optional<Site> mayBeSite = repository.findById(id);
        Site existSite = mayBeSite.get();

        Site siteBeforeUpdate = mapper.mapToSite(dto, existSite);

        repository.saveAndFlush(siteBeforeUpdate);
    }

}
