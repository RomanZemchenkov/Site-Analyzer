package searchengine.services.event_listeners.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import searchengine.dao.repository.RedisRepository;
import searchengine.services.IndexService;
import searchengine.services.PageService;
import searchengine.services.SiteService;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.event_listeners.IndexCreateEvent;
import searchengine.services.event_listeners.event.AnalyzedPageEvent;
import searchengine.services.event_listeners.event.CreatePageEvent;
import searchengine.services.event_listeners.event.FinishOrStopIndexingEvent;

@Component
@RequiredArgsConstructor
public class AnalyzerTaskListener {

    private final PageService pageService;
    private final SiteService siteService;
    private final RedisRepository redisRepository;
    private final IndexService indexService;

    @EventListener
    public void analyzedPageEventHandler(AnalyzedPageEvent event){
        CreatePageDto dto = event.getDto();
        pageService.createPage(dto);
    }

    @EventListener
    public void createPageEventHandler(CreatePageEvent event){
        UpdateSiteDto dto = event.getDto();
        siteService.updateSite(dto);
    }

    @EventListener
    public void finishOrStopIndexingEventHandler(FinishOrStopIndexingEvent event){
        String siteUrl = event.getSiteUrl();
        redisRepository.clearListByUrl(siteUrl);
    }

    @EventListener
    public void indexCreateEventHandler(IndexCreateEvent event){

    }
}
