package searchengine.services.searcher.analyzer.page_analyzer;

import org.springframework.http.HttpStatus;
import searchengine.dao.model.Status;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.event_listeners.event.CreatePageEvent;
import searchengine.services.event_listeners.event.UpdateSiteEvent;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.searcher.entity.HttpResponseEntity;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class PageAnalyzerTaskImpl implements PageAnalyzerTask {

    private final String pageUrl;
    private final PageParseContext context;
    private final EventPublisher publisher;

    public PageAnalyzerTaskImpl(String pageUrl, PageParseContext context, EventPublisher publisher) {
        this.pageUrl = pageUrl;
        this.context = context;
        this.publisher = publisher;
    }

    public HttpResponseEntity analyze() {
        HttpResponseEntity response = analyzeUrl();

        boolean isNormalResponse = checkStatusCode(response);
        if (!isNormalResponse) {
            ifErrorResponse(response.getContent());
        } else {
            ifNormalResponse(response);
        }
        return response;
    }

    public void stopAnalyze(ForkJoinPool usePool) {
        usePool.shutdownNow();
        try {
            if (!usePool.awaitTermination(60, TimeUnit.SECONDS)) {
                usePool.shutdownNow();
            }
        } catch (InterruptedException e) {
            usePool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void changeIfStopFlag(boolean flag) {
        PageParseContext.setIfStop(flag);
    }

    private HttpResponseEntity analyzeUrl() {
        PageAnalyzerImpl pageAnalyzerImpl = new PageAnalyzerImpl();
        return pageAnalyzerImpl.searchLink(pageUrl, context.getSite().getUrl());
    }


    private boolean checkStatusCode(HttpResponseEntity response) {
        HttpStatus status = HttpStatus.resolve(response.getStatusCode());
        return status != null && !status.is4xxClientError() && !status.is5xxServerError();
    }

    private void ifErrorResponse(String errorMessage) {
        System.out.println("Ошибка");
        updateSiteState(Status.FAILED.toString(), errorMessage);
    }

    private void createPageEvent(CreatePageDto createPageDto) {
        publisher.publishCreatePageEvent(new CreatePageEvent(createPageDto));
    }

    private void ifNormalResponse(HttpResponseEntity response) {
        ShowSiteDto site = context.getSite();
        String siteId = site.getId();

        Integer statusCode = response.getStatusCode();
        String urlPage = response.getUrl();
        String urlForSave = urlPage.substring(site.getUrl().length());

        String content = response.getContent();
        String code = String.valueOf(statusCode);

        CreatePageDto dto = new CreatePageDto(siteId, urlForSave, code, content);
        createPageEvent(dto);

        if (!PageParseContext.isIfStop()) {
            updateSiteState(Status.INDEXING.toString());
        }
    }


    public void updateSiteState(String status) {
        updateSiteState(status, null);
    }

    public void updateSiteState(String status, String content) {
        ShowSiteDto site = context.getSite();
        String mainUrl = site.getUrl();
        String siteName = site.getName();
        String siteId = site.getId();
        UpdateSiteDto siteDto;
        if (content != null) {
            siteDto = new UpdateSiteDto(siteId, status, content, mainUrl, siteName);
        } else {
            siteDto = new UpdateSiteDto(siteId, status, mainUrl, siteName);
        }
        publisher.publishUpdateSiteEvent(new UpdateSiteEvent(siteDto));
    }


}
