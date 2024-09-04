package searchengine.services.searcher;

import searchengine.dao.model.Status;
import searchengine.dao.repository.RedisRepository;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.event_listeners.event.AnalyzedPageEvent;
import searchengine.services.event_listeners.event.CreatePageEvent;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.searcher.entity.ErrorResponse;
import searchengine.services.searcher.entity.HttpResponseEntity;
import searchengine.services.searcher.entity.NormalResponse;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static searchengine.services.searcher.ConstantsCode.*;

public class SiteAnalyzerTask extends RecursiveAction {

    private final String url;
    private final ParseContext context;
    private final EventPublisher publisher;
    private final RedisRepository redis;

    public SiteAnalyzerTask(String url, ParseContext context, EventPublisher publisher, RedisRepository redis) {
        this.url = url;
        this.context = context;
        this.publisher = publisher;
        this.redis = redis;
    }

    @Override
    protected void compute() {
        final String mainUrl = context.getMainUrl();
        HttpResponseEntity response = analyzeUrl(mainUrl);

        boolean isNormalResponse = checkStatusCode(response);
        if (!isNormalResponse) {
            ifErrorResponse((ErrorResponse) response, context);
        } else {
            Set<String> newUrls = ifNormalResponse((NormalResponse) response, context);

            Set<String> availablePathForTask = checkAvailablePath(newUrls);

            createTask(availablePathForTask);
        }

    }

    private HttpResponseEntity analyzeUrl(String mainUrl) {
        PageAnalyzer pageAnalyzer = new PageAnalyzer(mainUrl);
        return pageAnalyzer.searchLink(url);
    }

    private Set<String> checkAvailablePath(Set<String> paths) {
        Set<String> availablePaths = new HashSet<>();
        String siteName = context.getSiteName();
        List<String> usePages = redis.getUsePages(siteName);
        for (String path : paths) {
            if (!usePages.contains(path)) {
                availablePaths.add(path);
                redis.saveUsePage(siteName, path);
            }
        }
        return availablePaths;
    }

    private boolean checkStatusCode(HttpResponseEntity response) {
        return !errorStatusCodes.contains(response.getStatusCode());
    }

    private void ifErrorResponse(ErrorResponse errorResponse, ParseContext context) {
        String content = errorResponse.getContent();
        ForkJoinPool pool = ForkJoinTask.getPool();
        stopIndexing(context, pool ,content);
    }

    private Set<String> ifNormalResponse(NormalResponse response, ParseContext context) {
        String siteId = context.getSiteId();

        Integer statusCode = response.getStatusCode();
        String pageUrl = response.getUrl();
        String content = response.getContent();
        String code = String.valueOf(statusCode);

        CreatePageDto dto = new CreatePageDto(siteId, pageUrl, code, content);
        publisher.publishEvent(new AnalyzedPageEvent(dto));

        updateSiteState(context);

        return response.getUrls();
    }

    private void createTask(Set<String> availablePathForTask){
        Set<SiteAnalyzerTask> futureTaskSet = new HashSet<>();

        for (String path : availablePathForTask) {
            SiteAnalyzerTask newTask = context.getFactory().createTask(path, context);
            futureTaskSet.add(newTask);
        }

        for (SiteAnalyzerTask task : futureTaskSet) {
            task.fork();
        }

        for (SiteAnalyzerTask task : futureTaskSet) {
            task.join();
        }
    }

    private void updateSiteState(ParseContext context){
        if(!INDEX_STOP_GLOBAL_FLAG){
            String mainUrl = context.getMainUrl();
            String siteName = context.getSiteName();
            String siteId = context.getSiteId();
            UpdateSiteDto siteDto = new UpdateSiteDto(siteId, Status.INDEXING.toString(), mainUrl, siteName);
            publisher.publishUpdateSiteEvent(new CreatePageEvent(siteDto));
        }
    }

    public void stopIndexing(ParseContext context, ForkJoinPool usePool, String content){
        String mainUrl = context.getMainUrl();
        String siteName = context.getSiteName();
        System.out.println(usePool);
        String siteId = context.getSiteId();
        usePool.shutdownNow();

        UpdateSiteDto dto = new UpdateSiteDto(siteId, Status.FAILED.toString(), content, mainUrl, siteName);
        CreatePageEvent createPageEvent = new CreatePageEvent(dto);
        publisher.publishUpdateSiteEvent(createPageEvent);
    }
}
