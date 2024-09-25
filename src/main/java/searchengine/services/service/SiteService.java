package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.dao.repository.index.IndexRepository;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.dao.repository.page.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.mapper.SiteMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteService {

    private final SiteRepository repository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final StatisticRepository statisticRepository;
    private final SiteMapper mapper;
    private final SiteRepository siteRepository;

    @Transactional
    public Integer createSite(CreateSiteDto dto){
        System.out.println("Новый сайт создан или удалён начало");
        String name = dto.getName();

        Optional<Site> mayBeSite = repository.findSiteByName(name);

        time( () -> mayBeSite.ifPresent(this::deleteAll));

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

    private void deleteAll(Site site){
        List<Page> pagesBySite = pageRepository.findAllBySite(site);
        timeForIndex(() -> pagesBySite.forEach(indexRepository::deleteAllByPage));
        timeForLemma(() -> lemmaRepository.deleteAllBySite(site));
        statisticRepository.deleteAllBySite(site);
        pageRepository.deleteAllBySite(site);
        siteRepository.deleteSite(site);
    }

    static void timeForLemma(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Леммы удалены за: " + (finish - start));
    }

    static void timeForIndex(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Индексы удалены за: " + (finish - start));
    }

    static void time(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Метод отработал за: " + (finish - start));
    }

}
