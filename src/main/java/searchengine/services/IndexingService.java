package searchengine.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.services.dto.SiteProperties;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.event_listeners.event.FinishOrStopIndexingEvent;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.searcher.ParseContext;
import searchengine.services.searcher.SiteAnalyzerTask;
import searchengine.services.searcher.SiteAnalyzerTaskFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static searchengine.services.searcher.ConstantsCode.*;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private final SiteService siteService;
    private final SiteAnalyzerTaskFactory factory;
    private final SiteProperties properties;
    private final EventPublisher publisher;
    private Map<String, String> sitesAndNames;
    private List<ParseContext> contexts;
    private List<SiteAnalyzerTask> firstTasksList;
    private Queue<ForkJoinPool> pools = new ArrayDeque<>();


    @PostConstruct
    private void initProperties(){
        List<SiteProperties.Site> sites = properties.getSites();
        sitesAndNames = sites.stream()
                .collect(Collectors.toMap(SiteProperties.Site::getName,SiteProperties.Site::getUrl));
    }

    public void startIndexing() {
        createContext();

        firstTasksList = new ArrayList<>();
        for (ParseContext context : contexts) {
            String startUrl = context.getMainUrl();
            firstTasksList.add(factory.createTask(startUrl, context));
        }

        executeTasks();

        clearRedisKeys();
    }

    public void stopIndexing(){
        INDEX_STOP_GLOBAL_FLAG = true;
        for(int i = 0; i < firstTasksList.size(); i++){
            SiteAnalyzerTask task = firstTasksList.get(i);
            ParseContext context = contexts.get(i);
            ForkJoinPool forkJoinPool = pools.poll();
            task.stopIndexing(context, forkJoinPool, STOP_INDEXING_TEXT);
        }
    }

    private void executeTasks(){
        int countOfFirstTask = firstTasksList.size();

        ExecutorService threadPool = Executors.newFixedThreadPool(countOfFirstTask);
        for (int i = 0; i < countOfFirstTask; i++) {
            int countOfProcessors = Runtime.getRuntime().availableProcessors();
            int processorsForOneTask = countOfProcessors / countOfFirstTask;
            ForkJoinPool forkJoinPool = new ForkJoinPool(processorsForOneTask);
            SiteAnalyzerTask task = firstTasksList.get(i);
            pools.add(forkJoinPool);
            threadPool.submit(() -> {
                forkJoinPool.invoke(task);
                forkJoinPool.shutdown();
            });
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void createContext(){
        contexts = new ArrayList<>();

        for (Map.Entry<String, String> entry : sitesAndNames.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();

            CreateSiteDto dto = new CreateSiteDto(url, name);
            String siteId = String.valueOf(siteService.createSite(dto));

            ParseContext context = new ParseContext(siteId, name, url, factory);
            contexts.add(context);
        }
    }

    private void clearRedisKeys(){
        for(ParseContext context : contexts){
            String siteUrl = context.getMainUrl();
            FinishOrStopIndexingEvent event = new FinishOrStopIndexingEvent(siteUrl);
            publisher.publishFinishAndStopIndexingEvent(event);
        }
    }
}
