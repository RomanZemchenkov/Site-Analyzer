package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.page.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.mapper.PageMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final PageMapper mapper;

    @Transactional()
    public Page createPage(CreatePageDto dto) {
        Integer siteId = Integer.valueOf(dto.getSiteId());
        deletePageIfExist(dto.getPath(), siteId);

        Site site = siteRepository.findById(siteId).get();

        Page page = mapper.mapToPage(dto, site);

        return pageRepository.save(page);
    }

    public FindPageDto findPageWithSite(String pageUrl, Integer siteId) {
        Page page = pageRepository.findByPathAndSiteId(pageUrl, siteId).get();
        return new FindPageDto(page, page.getSite());
    }

    private void deletePageIfExist(String pageUri, Integer siteId) {
        Optional<Page> mayBePage = pageRepository.findByPathAndSiteId(pageUri, siteId);
        mayBePage.ifPresent(pageRepository::delete);
    }

}
