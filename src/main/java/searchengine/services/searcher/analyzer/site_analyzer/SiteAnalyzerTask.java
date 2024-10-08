package searchengine.services.searcher.analyzer.site_analyzer;

import lombok.ToString;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.searcher.analyzer.page_analyzer.AnalyzeResponse;
import searchengine.services.searcher.analyzer.page_analyzer.ErrorAnalyzeResponse;
import searchengine.services.searcher.analyzer.page_analyzer.NormalAnalyzeResponse;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTask;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTaskFactory;
import searchengine.services.searcher.analyzer.page_analyzer.PageParseContext;

import java.util.*;
import java.util.concurrent.*;

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
        AnalyzeResponse response;
        try {
            response = createPageAnalyzerTask();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (!context.isIfErrorResponse()) {
            NormalAnalyzeResponse normalResponse = (NormalAnalyzeResponse) response;
            Set<String> availablePath = checkAvailablePath(normalResponse.getUrls());
            context.getPagesSet().add(normalResponse.getPage());
            createTask(availablePath);
        }
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

    private AnalyzeResponse createPageAnalyzerTask() throws ExecutionException, InterruptedException {
        PageParseContext pageParseContext = new PageParseContext(context.getSite());
        pageAnalyzerTaskImpl = pageAnalyzerTaskFactory.createTask(pageUrl, pageParseContext);

        AnalyzeResponse analyzeResponse = pageAnalyzerTaskImpl.analyze();
        checkAnalyzeResponse(analyzeResponse);
        return analyzeResponse;
    }

    private void checkAnalyzeResponse(AnalyzeResponse analyzeResponse) {
        if (analyzeResponse instanceof ErrorAnalyzeResponse) {
            ForkJoinPool currentPool = ForkJoinTask.getPool();
            stopAnalyze(currentPool);
            context.setErrorContent(((ErrorAnalyzeResponse) analyzeResponse).getContent());
            context.setIfErrorResponse(true);
        }
    }

    private Set<String> checkAvailablePath(Set<String> paths) {
        Set<String> availablePaths = new HashSet<>();
        for (String path : paths) {
            if (!useUrlsSet.contains(path)) {
                availablePaths.add(path);
                useUrlsSet.add(path);
            }
        }
        return availablePaths;
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
