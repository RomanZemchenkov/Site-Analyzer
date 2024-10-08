package searchengine.services.searcher.analyzer.page_analyzer;

import org.springframework.http.HttpStatus;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.site.ShowSiteDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.searcher.entity.HttpResponseEntity;
import searchengine.services.searcher.entity.NormalResponse;
import searchengine.services.service.PageService;
import searchengine.services.service.SiteService;

public class PageAnalyzerTaskImpl implements PageAnalyzerTask {

    private final String pageUrl;
    private final PageParseContext context;
    private final SiteService siteService;
    private final PageService pageService;

    public PageAnalyzerTaskImpl(String pageUrl, PageParseContext context, SiteService siteService, PageService pageService) {
        this.pageUrl = pageUrl;
        this.context = context;
        this.siteService = siteService;
        this.pageService = pageService;
    }

    public AnalyzeResponse analyze() {
        HttpResponseEntity response = analyzeUrl();

        boolean isNormalResponse = checkStatusCode(response);
        AnalyzeResponse analyzeResponse;
        if (!isNormalResponse) {
            analyzeResponse = ifErrorResponse(response);
        } else {
            analyzeResponse = ifNormalResponse(response);
        }
        return analyzeResponse;
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

    private AnalyzeResponse ifErrorResponse(HttpResponseEntity response) {
        updateSiteState(Status.FAILED.toString(), response.getContent());
        return new ErrorAnalyzeResponse(response.getContent());
    }


    private AnalyzeResponse ifNormalResponse(HttpResponseEntity response) {
        Site site = context.getSite();
        String siteId = String.valueOf(site.getId());

        Integer statusCode = response.getStatusCode();
        String urlPage = response.getUrl();
        String urlForSave = urlPage.substring(site.getUrl().length());

        String content = response.getContent();
        String code = String.valueOf(statusCode);

        CreatePageDto dto = new CreatePageDto(siteId, urlForSave, code, content);
        Page savedPage = pageService.createPage(dto);

        if (!PageParseContext.isIfStop()) {
            updateSiteState(Status.INDEXING.toString());
        }
        return new NormalAnalyzeResponse(savedPage, ((NormalResponse) response).getUrls());
    }


    public void updateSiteState(String status) {
        updateSiteState(status, null);
    }

    public void updateSiteState(String status, String content) {
        Site site = context.getSite();
        String mainUrl = site.getUrl();
        String siteName = site.getName();
        String siteId = String.valueOf(site.getId());
        UpdateSiteDto siteDto;
        if (content != null) {
            siteDto = new UpdateSiteDto(siteId, status, content, mainUrl, siteName);
        } else {
            siteDto = new UpdateSiteDto(siteId, status, mainUrl, siteName);
        }
        siteService.updateSite(siteDto);
    }


}
