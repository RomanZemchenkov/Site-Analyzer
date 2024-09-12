package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.dao.repository.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.page.CreatePageWithMainSiteUrlDto;
import searchengine.services.dto.page.CreatedPageInfoDto;
import searchengine.services.mapper.PageMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final PageMapper mapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public String createPage(CreatePageDto dto){

        Integer siteId = Integer.valueOf(dto.getSiteId());

        Site site = siteRepository.findById(siteId).get();

        Page page = mapper.mapToPage(dto, site);

        Page savedPage = pageRepository.save(page);
        return savedPage.getPath();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CreatedPageInfoDto createPage(CreatePageWithMainSiteUrlDto dto){
        String path = dto.getPageUrl();
        checkExistPage(path);

        String siteUrl = dto.getSiteUrl();
        String siteName = dto.getSiteName();

        Optional<Site> mayBeExistSite = siteRepository.findSiteByUrl(siteUrl);
        Site site;
        if (mayBeExistSite.isEmpty()){
            site = siteRepository.save(new Site(Status.INDEXING, OffsetDateTime.now(ZoneId.systemDefault()),"",siteUrl,siteName));
        } else {
            site = mayBeExistSite.get();
            site.setStatus(Status.INDEXING);
        }

        Page page = mapper.mapToPage(dto, site);

        Page savedPage = pageRepository.save(page);
        return new CreatedPageInfoDto(savedPage,site);
    }

    private void checkExistPage(String pageUrl){
        Optional<Page> mayBePage = pageRepository.findByPath(pageUrl);
        mayBePage.ifPresent(pageRepository::delete);
    }

}
