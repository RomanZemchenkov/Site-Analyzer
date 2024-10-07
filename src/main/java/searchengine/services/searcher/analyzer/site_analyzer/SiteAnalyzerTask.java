package searchengine.services.searcher.analyzer.site_analyzer;

import lombok.ToString;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTask;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTaskFactory;
import searchengine.services.searcher.analyzer.page_analyzer.PageParseContext;
import searchengine.services.searcher.entity.ErrorResponse;
import searchengine.services.searcher.entity.HttpResponseEntity;
import searchengine.services.searcher.entity.NormalResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


import static searchengine.services.GlobalVariables.STOP_INDEXING_TEXT;

@ToString
public class SiteAnalyzerTask extends RecursiveAction {

    private final String pageUrl;
    private final ParseContext context;
    private final EventPublisher publisher;
    private final ConcurrentSkipListSet<String> useUrlsSet;
    private final PageAnalyzerTaskFactory pageAnalyzerTaskFactory;
    private PageAnalyzerTask pageAnalyzerTaskImpl;

    public SiteAnalyzerTask(String pageUrl, ParseContext context, EventPublisher publisher, ConcurrentSkipListSet<String> useUrlsSet, PageAnalyzerTaskFactory pageAnalyzerTaskFactory) {
        this.pageUrl = pageUrl;
        this.context = context;
        this.publisher = publisher;
        this.useUrlsSet = useUrlsSet;
        this.pageAnalyzerTaskFactory = pageAnalyzerTaskFactory;
    }

    @Override
    protected void compute() {
        HttpResponseEntity response;
        try {
            response = createPageAnalyzerTask();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (!context.isIfErrorResponse()) {
            Set<String> availablePath = checkAvailablePath(((NormalResponse) response).getUrls());
            createTask(availablePath);
        }
    }

    public void stopIndexing(ForkJoinPool usePool) {
        stopAnalyze(usePool);
        pageAnalyzerTaskImpl.changeIfStopFlag(true);
        context.setErrorContent(STOP_INDEXING_TEXT);
    }

    public void updateSiteState(String status) {
        pageAnalyzerTaskImpl.updateSiteState(status);
    }

    public void updateSiteState(String status, String content) {
        pageAnalyzerTaskImpl.updateSiteState(status, content);
    }

    private HttpResponseEntity createPageAnalyzerTask() throws ExecutionException, InterruptedException {
        PageParseContext pageParseContext = new PageParseContext(context.getSiteDto());
        pageAnalyzerTaskImpl = pageAnalyzerTaskFactory.createTask(pageUrl, pageParseContext);

        HttpResponseEntity analyze = pageAnalyzerTaskImpl.analyze();
        checkResponse(analyze);
        return analyze;
    }

    private void checkResponse(HttpResponseEntity httpResponseEntity) {
        if (httpResponseEntity instanceof ErrorResponse) {
            ForkJoinPool currentPool = ForkJoinTask.getPool();
            stopAnalyze(currentPool);
            context.setErrorContent(httpResponseEntity.getContent());
            context.setIfErrorResponse(true);
        }
    }

    private Set<String> checkAvailablePath(Set<String> paths) {
        Set<String> availablePaths = new HashSet<>();
        for (String path : paths) {
            if (hasAlreadyExistPath(path)) {
                availablePaths.add(path);
                useUrlsSet.add(path);
            }
        }
        return availablePaths;
    }

    private boolean hasAlreadyExistPath(String path) {
        List<String> usePages = useUrlsSet.stream().toList();
        for (String usePath : usePages) {
            if (path.equals(usePath)) {
                return false;
            }
        }
        return true;
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
            if(!task.isCancelled()){
                task.join();
            }
        }
    }

    private void stopAnalyze(ForkJoinPool usePool) {
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


}
