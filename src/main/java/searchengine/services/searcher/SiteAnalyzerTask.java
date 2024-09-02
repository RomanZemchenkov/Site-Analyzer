package searchengine.services.searcher;

import searchengine.dao.model.Page;
import searchengine.dao.model.Status;
import searchengine.dao.repository.PageRepository;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.site.UpdateSiteDto;
import searchengine.services.event_listeners.event.AnalyzedPageEvent;
import searchengine.services.event_listeners.event.CreatePageEvent;
import searchengine.services.event_listeners.publisher.EventPublisher;

import java.util.*;
import java.util.concurrent.RecursiveAction;

import static searchengine.services.searcher.ConstantsCode.*;

public class SiteAnalyzerTask extends RecursiveAction {

    private final String url;
    private final ParseContext context;
    private final EventPublisher publisher;
    private final PageRepository repository;
    private final Set<String> useUrls;

    public SiteAnalyzerTask(String url, ParseContext context, EventPublisher publisher, PageRepository repository, Set<String> useUrls) {
        this.url = url;
        this.context = context;
        this.publisher = publisher;
        this.repository = repository;
        this.useUrls = useUrls;
    }
    @Override
    protected void compute() {
        final String mainUrl = context.getMainUrl();

        ResponseEntity responseEntity = analyzeUrl(mainUrl);

        Set<String> newUrls = checkStatusCode(responseEntity, context);

        Set<String> availablePathForTask = checkAvailablePath(newUrls);

        Set<SiteAnalyzerTask> futureTaskSet = new HashSet<>();

        for(String path : availablePathForTask){
            SiteAnalyzerTask newTask = context.getFactory().createTask(path, context);
            futureTaskSet.add(newTask);
        }

        for (SiteAnalyzerTask task : futureTaskSet){
            task.fork();
            task.join();
        }
    }

    private ResponseEntity analyzeUrl(String mainUrl){
        PageAnalyzer pageAnalyzer = new PageAnalyzer(mainUrl);
        return pageAnalyzer.searchLink(url);
    }

//    private Set<String> checkAvailablePath(Set<String> paths){
//        Set<String> availablePaths = new HashSet<>();
//        for(String path : paths){
//            Optional<Page> mayBePath = repository.findByPath(path);
//            if(mayBePath.isEmpty()){
//                availablePaths.add(path);
//            }
//        }
//        return availablePaths;
//    }

    private Set<String> checkAvailablePath(Set<String> paths){
        Set<String> availablePaths = new HashSet<>();
        for(String path : paths){
            if(!useUrls.contains(path)){
                availablePaths.add(path);
                useUrls.add(path);
            }
        }
        return availablePaths;
    }

    private Set<String> checkStatusCode(ResponseEntity responseEntity, ParseContext context){
        Integer statusCode = responseEntity.getStatusCode();

        if(errorStatusCodes.contains(statusCode)){
            publishEvent(responseEntity,context);
            throw new RuntimeException("Error");
        } else {
            publishEvent(responseEntity,context);
            return responseEntity.getUrls();
        }
    }

    private void publishEvent(ResponseEntity responseEntity, ParseContext context){
        Integer statusCode = responseEntity.getStatusCode();
        String pageUrl = responseEntity.getUrl();
        String content = responseEntity.getContent();
        String code = String.valueOf(statusCode);

        String mainUrl = context.getMainUrl();
        String siteId = context.getSiteId();
        String siteName = context.getSiteName();

        CreatePageDto createdDto = new CreatePageDto(siteId, pageUrl, code, content);
        publisher.publishEvent(new AnalyzedPageEvent(createdDto));

        UpdateSiteDto siteDto = new UpdateSiteDto(siteId, Status.INDEXING.toString(), "error", mainUrl, siteName);
        publisher.publicUpdateSiteEvent(new CreatePageEvent(siteDto));
    }

}
