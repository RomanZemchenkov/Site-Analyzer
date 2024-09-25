package searchengine.services.event_listeners.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import searchengine.services.service.PageService;
import searchengine.services.service.SiteService;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.event_listeners.event.CreatePageEvent;
import searchengine.services.event_listeners.event.UpdateSiteEvent;

@Component
@RequiredArgsConstructor
public class AnalyzerTaskListener {

    private final PageService pageService;
    private final SiteService siteService;

    @EventListener
    public void createPageEventHandler(CreatePageEvent event){
        CreatePageDto dto = event.getDto();
        pageService.createPage(dto);
    }

    @EventListener
    public void updateSiteEventHandler(UpdateSiteEvent event){
        UpdateSiteDto dto = event.getDto();
        siteService.updateSite(dto);
    }
}
