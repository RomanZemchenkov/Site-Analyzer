package searchengine.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.PageRepository;
import searchengine.dao.repository.SiteRepository;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.mapper.PageMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageService {

    private final PageRepository repository;
    private final SiteRepository siteRepository;
    private final PageMapper mapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public String createPage(CreatePageDto dto){

        Integer siteId = Integer.valueOf(dto.getSiteId());

        Site site = siteRepository.findById(siteId).get();

        Page page = mapper.mapToPage(dto, site);

        Page savedPage = repository.save(page);
        return savedPage.getPath();
    }
}
