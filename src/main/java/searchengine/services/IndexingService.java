package searchengine.services;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Status;
import searchengine.services.dto.SiteProperties;
import searchengine.services.dto.page.CreatePageWithMainSiteUrlDto;
import searchengine.services.dto.page.CreatedPageInfoDto;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.event_listeners.event.FinishOrStopIndexingEvent;
import searchengine.services.event_listeners.publisher.EventPublisher;
import searchengine.services.exception.IllegalPageException;
import searchengine.services.searcher.indexing.PageAnalyzer;
import searchengine.services.searcher.indexing.ParseContext;
import searchengine.services.searcher.indexing.SiteAnalyzerTask;
import searchengine.services.searcher.indexing.SiteAnalyzerTaskFactory;
import searchengine.services.searcher.entity.HttpResponseEntity;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static searchengine.services.searcher.GlobalVariables.*;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private final SiteService siteService;
    private final PageService pageService;
    private final SiteAnalyzerTaskFactory factory;
    private final SiteProperties properties;
    private final EventPublisher publisher;
    @Getter
    private Map<String, String> namesAndSites;
    private List<ParseContext> contexts;
    private List<SiteAnalyzerTask> firstTasksList;
    private HashMap<SiteAnalyzerTask, ForkJoinPool> pools = new HashMap<>();


    @PostConstruct
    private void initProperties() {
        List<SiteProperties.Site> sites = properties.getSites();
        namesAndSites = sites.stream()
                .collect(Collectors.toMap(SiteProperties.Site::getName, SiteProperties.Site::getUrl));
    }

    public void startIndexing() {
        INDEXING_STARTED = true;
        createContext();

        firstTasksList = new ArrayList<>();
        for (ParseContext context : contexts) {
            String startUrl = context.getMainUrl();
            firstTasksList.add(factory.createTask(startUrl, context, new ConcurrentSkipListSet<>()));
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
        System.out.println("Остановка задачи");
        for (ParseContext context : contexts) {
            context.setIndexingStopFlag(true);
        }

        for (Map.Entry<SiteAnalyzerTask, ForkJoinPool> entry : pools.entrySet()) {
            SiteAnalyzerTask task = entry.getKey();
            ForkJoinPool pool = entry.getValue();
            task.stopIndexing(pool, STOP_INDEXING_TEXT);
        }
    }

    public CreatedPageInfoDto onePageIndexing(FindPageDto dto) {
        String pageUrl = dto.getUrl();
        String siteUrl = "";
        String siteName = "";
        for (Map.Entry<String, String> entry : namesAndSites.entrySet()) {
            String url = entry.getValue();
            String name = entry.getKey();
            if (pageUrl.startsWith(url)) {
                siteUrl = url;
                siteName = name;
                break;
            }
        }
        if (siteUrl.isEmpty()) {
            throw new IllegalPageException();
        }
        PageAnalyzer analyzer = new PageAnalyzer(pageUrl);
        HttpResponseEntity response = analyzer.searchLink(pageUrl);
        CreatePageWithMainSiteUrlDto page = new CreatePageWithMainSiteUrlDto(siteUrl,siteName, pageUrl, String.valueOf(response.getStatusCode()), response.getContent());
        CreatedPageInfoDto infoDto = pageService.createPage(page);
        infoDto.getSite().setStatus(Status.INDEXED);
        return infoDto;
    }

    private void executeTask(SiteAnalyzerTask task, ParseContext context, int countOfParallel) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(countOfParallel);
        pools.put(task, forkJoinPool);
        System.out.printf("Для задачи %s назначается пул %s\n", task, forkJoinPool);
        try {
            forkJoinPool.invoke(task);
        } finally {
            forkJoinPool.shutdown();
            if (!context.isIndexingStopFlag()) {
                task.updateSiteState(Status.INDEXED.toString());
            }
            clearRedisKeys(context.getSiteName());
            System.out.printf("Поток %s закончил свою работу\n", Thread.currentThread().getName());
        }
    }


    private void createContext() {
        contexts = new ArrayList<>();

        for (Map.Entry<String, String> entry : namesAndSites.entrySet()) {
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
