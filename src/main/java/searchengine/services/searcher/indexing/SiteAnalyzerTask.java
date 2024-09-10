package searchengine.services.searcher.indexing;

import lombok.ToString;
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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static searchengine.services.searcher.GlobalVariables.*;

@ToString
public class SiteAnalyzerTask extends RecursiveAction {

    private final String url;
    private final ParseContext context;
    private final EventPublisher publisher;
    private final RedisRepository redis;
    private final ConcurrentSkipListSet<String> useUrlsSet;

    public SiteAnalyzerTask(String url, ParseContext context, EventPublisher publisher, RedisRepository redis, ConcurrentSkipListSet<String> useUrlsSet) {
        this.url = url;
        this.context = context;
        this.publisher = publisher;
        this.redis = redis;
        this.useUrlsSet = useUrlsSet;
    }

    @Override
    protected void compute() {
        final String mainUrl = context.getMainUrl();
        HttpResponseEntity response = analyzeUrl(mainUrl);

        boolean isNormalResponse = checkStatusCode(response);
        if (!isNormalResponse) {
            ifErrorResponse((ErrorResponse) response);
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
        for (String path : paths) {
            if (hasAlreadyExistPath(siteName,path)) {
                availablePaths.add(path);
//                redis.saveUsePage(siteName, path);
                useUrlsSet.add(path);
            }
        }
        return availablePaths;
    }

    private boolean hasAlreadyExistPath(String siteName, String path){
//        List<String> usePages = redis.getUsePages(siteName);
        List<String> usePages = useUrlsSet.stream().toList();
        for(String usePath : usePages){
            if(path.equals(usePath)){
                return false;
            }
        }
        return true;
    }

    private boolean checkStatusCode(HttpResponseEntity response) {
        return !errorStatusCodes.contains(response.getStatusCode());
    }

    private void ifErrorResponse(ErrorResponse errorResponse) {
        String content = errorResponse.getContent();
        context.setIndexingStopFlag(true);
        ForkJoinPool pool = ForkJoinTask.getPool();
        stop(pool, content);
    }

    private Set<String> ifNormalResponse(NormalResponse response, ParseContext context) {
        String siteId = context.getSiteId();

        Integer statusCode = response.getStatusCode();
        String urlPage = response.getUrl();
        String urlForSave = urlPage.substring(context.getMainUrl().length());
        String content = response.getContent();
        String code = String.valueOf(statusCode);


        CreatePageDto dto = new CreatePageDto(siteId, urlForSave, code, content);
        publisher.publishEvent(new AnalyzedPageEvent(dto));
//        redis.saveUsePage(context.getSiteName(), urlPage);
        useUrlsSet.add(urlPage);

        if (!context.isIndexingStopFlag()) {
            updateSiteState(Status.INDEXING.toString());
        }

        return response.getUrls();
    }

    private void createTask(Set<String> availablePathForTask) {
        Set<SiteAnalyzerTask> futureTaskSet = new HashSet<>();

        for (String path : availablePathForTask) {
            SiteAnalyzerTask newTask = context.getFactory().createTask(path, context, useUrlsSet);
            futureTaskSet.add(newTask);
        }

        for (SiteAnalyzerTask task : futureTaskSet) {
            task.fork();
        }

        for (SiteAnalyzerTask task : futureTaskSet) {
            task.join();
        }
    }

    public void updateSiteState(String status) {
        String mainUrl = context.getMainUrl();
        String siteName = context.getSiteName();
        String siteId = context.getSiteId();
        UpdateSiteDto siteDto = new UpdateSiteDto(siteId, status, mainUrl, siteName);
        publisher.publishUpdateSiteEvent(new CreatePageEvent(siteDto));
    }

    public void stopIndexing(ForkJoinPool usePool, String content) {
        stop(usePool, content);
    }

    private void stop(ForkJoinPool usePool, String content) {
        String mainUrl = context.getMainUrl();
        String siteName = context.getSiteName();
        String siteId = context.getSiteId();
        usePool.shutdownNow();

        UpdateSiteDto dto = new UpdateSiteDto(siteId, Status.FAILED.toString(), content, mainUrl, siteName);
        CreatePageEvent createPageEvent = new CreatePageEvent(dto);
        publisher.publishUpdateSiteEvent(createPageEvent);
    }


}
