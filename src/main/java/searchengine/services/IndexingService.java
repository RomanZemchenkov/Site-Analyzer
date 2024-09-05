package searchengine.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Status;
import searchengine.services.dto.SiteProperties;
import searchengine.services.dto.page.FindPageDto;
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

import static searchengine.services.searcher.GlobalVariables.*;

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
    private HashMap<SiteAnalyzerTask,ForkJoinPool> pools = new HashMap<>();


    @PostConstruct
    private void initProperties() {
        List<SiteProperties.Site> sites = properties.getSites();
        sitesAndNames = sites.stream()
                .collect(Collectors.toMap(SiteProperties.Site::getName, SiteProperties.Site::getUrl));
    }

    public void startIndexing() {
        INDEXING_STARTED = true;
        createContext();

        firstTasksList = new ArrayList<>();
        for (ParseContext context : contexts) {
            String startUrl = context.getMainUrl();
            firstTasksList.add(factory.createTask(startUrl, context));
        }

        int availableProcessors = Runtime.getRuntime().availableProcessors();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < firstTasksList.size(); i++) {
            SiteAnalyzerTask task = firstTasksList.get(i);
            ParseContext context = contexts.get(i);
            int countOfParallel = Math.max(1, availableProcessors / firstTasksList.size());
            threadPool.submit(() -> executeTask(task, context, countOfParallel));
        }
        threadPool.shutdown();


        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            pools.clear();
            INDEXING_STARTED = false;
        }

    }

    public void stopIndexing() {
        INDEX_STOP_GLOBAL_FLAG = true;
        System.out.println("Список всех контекстов: " + contexts);
        for(ParseContext context : contexts){
            context.setIndexingStopFlag(true);
        }

        for(Map.Entry<SiteAnalyzerTask, ForkJoinPool> entry : pools.entrySet()){
            SiteAnalyzerTask task = entry.getKey();
            ForkJoinPool pool = entry.getValue();
            System.out.println("Для остановки задачи " + task + " мы передаём пул: " + pool);
            task.stopIndexing(pool,STOP_INDEXING_TEXT);
        }
    }

    public void onePageIndexing(FindPageDto dto) {
        
    }

    private void executeTask(SiteAnalyzerTask task, ParseContext context, int countOfParallel) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(countOfParallel);
        pools.put(task,forkJoinPool);
        System.out.printf("Для задачи %s назначается пул %s\n", task,forkJoinPool);
        try {
            forkJoinPool.invoke(task);
        } finally {
            forkJoinPool.shutdown();
            if(!context.isIndexingStopFlag()){
                task.updateSiteState(Status.INDEXED.toString());
            }
            clearRedisKeys(context.getSiteName());
            System.out.printf("Поток %s закончил свою работу\n", Thread.currentThread().getName());
        }
    }


    private void createContext() {
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

    private void clearRedisKeys(String siteUrl) {
        FinishOrStopIndexingEvent event = new FinishOrStopIndexingEvent(siteUrl);
        publisher.publishFinishAndStopIndexingEvent(event);
    }


}
