package searchengine.services.searcher.analyzer;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.model.Status;
import searchengine.services.searcher.analyzer.page_analyzer.AnalyzeResponse;
import searchengine.services.searcher.analyzer.page_analyzer.ErrorAnalyzeResponse;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTask;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerTaskFactory;
import searchengine.services.searcher.analyzer.page_analyzer.PageParseContext;
import searchengine.services.searcher.analyzer.site_analyzer.ParseContext;
import searchengine.services.searcher.analyzer.site_analyzer.SiteAnalyzerTask;
import searchengine.services.searcher.analyzer.site_analyzer.SiteAnalyzerTaskFactory;
import searchengine.services.service.PageService;
import searchengine.services.service.SiteService;
import searchengine.services.dto.SiteProperties;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.exception.IllegalPageException;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static searchengine.services.GlobalVariables.*;

@Service
@RequiredArgsConstructor
public class IndexingImpl implements Indexing {

    private final SiteService siteService;
    private final PageService pageService;
    private final SiteAnalyzerTaskFactory factory;
    private final PageAnalyzerTaskFactory pageFactory;
    private final SiteProperties properties;
    @Getter
    private Map<String, String> namesAndSites;
    private List<ParseContext> contexts;
    private HashMap<SiteAnalyzerTask, ForkJoinPool> pools = new HashMap<>();


    @PostConstruct
    private void initProperties() {
        List<SiteProperties.Site> sites = properties.getSites();
        namesAndSites = sites.stream()
                .collect(Collectors.toMap(SiteProperties.Site::getName, SiteProperties.Site::getUrl));
    }

    @CheckTimeWorking
    public HashMap<Site, List<Page>> startSitesIndexing() {
        INDEXING_STARTED = true;
        createContext();

        List<SiteAnalyzerTask> firstTasksList = new ArrayList<>();
        for (ParseContext context : contexts) {
            String startUrl = context.getSite().getUrl();
            ConcurrentSkipListSet<String> useUrlsSet = new ConcurrentSkipListSet<>();
            useUrlsSet.add(startUrl);
            firstTasksList.add(factory.createTask(startUrl, context, useUrlsSet));
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();
        HashMap<Site, List<Page>> sitesAndPages = new HashMap<>();
        for (int i = 0; i < firstTasksList.size(); i++) {
            SiteAnalyzerTask task = firstTasksList.get(i);
            ParseContext context = contexts.get(i);
            int countOfParallel = Math.max(1, COUNT_OF_PROCESSORS / firstTasksList.size());
            Future<List<Page>> future = threadPool.submit(() -> executeTask(task, context, countOfParallel));
            try {
                List<Page> pages = future.get();
                sitesAndPages.put(context.getSite(),pages);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        threadPool.shutdown();


        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
            System.out.println("Внутри индексатора");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            pools.clear();
            INDEXING_STARTED = false;
        }

        return sitesAndPages;
    }

    public void stopIndexing() {
        System.out.println("Остановка задачи");
        for (ParseContext context : contexts) {
            context.setIfErrorResponse(true);
        }

        Map<SiteAnalyzerTask, ForkJoinPool> localPools = new HashMap<>(pools);
        for (Map.Entry<SiteAnalyzerTask, ForkJoinPool> entry : localPools.entrySet()) {
            SiteAnalyzerTask task = entry.getKey();
            ForkJoinPool pool = entry.getValue();
            task.stopIndexing(pool);
        }
    }

    private List<Page> executeTask(SiteAnalyzerTask task, ParseContext context, int countOfParallel) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(countOfParallel);
        pools.put(task, forkJoinPool);
        try {
            forkJoinPool.invoke(task);
        } finally {
            forkJoinPool.shutdown();
            try {
                if (!forkJoinPool.awaitTermination(1, TimeUnit.HOURS)) {
                    forkJoinPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                forkJoinPool.shutdownNow();
            }
            if (!context.isIfErrorResponse()) {
                System.out.println("Устанавливаем окончание работы таски без ошибок");
                task.updateSiteState(Status.INDEXED.toString());
            } else {
                System.out.println("Устанавливаем окончание работы таски с ошибкой");
                task.updateSiteState(Status.FAILED.toString(), context.getErrorContent());
            }
        }
        return context.getPagesSet().stream().toList();
    }

    private void createContext() {
        System.out.println("Контекст создаётся начало");
        contexts = new ArrayList<>();

        for (Map.Entry<String, String> entry : namesAndSites.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();

            CreateSiteDto dto = new CreateSiteDto(url, name);
            Site site = siteService.createSite(dto);

            ParseContext context = new ParseContext(site, factory);
            contexts.add(context);
        }
        System.out.println("Контекст создаётся конец");

    }

    public FindPageDto startPageIndexing(String searchedUrl) {
        System.out.println("Выполнение задачи");
        Site site = checkSiteExist(searchedUrl);
        PageParseContext pageContext = new PageParseContext(site);
        PageAnalyzerTask task = pageFactory.createTask(searchedUrl, pageContext);
        AnalyzeResponse analyzeResult = task.analyze();

        String pageUri = searchedUrl.substring(site.getUrl().length());
        System.out.println("сохранена " + pageUri);
        if (analyzeResult instanceof ErrorAnalyzeResponse) {
            task.updateSiteState(Status.FAILED.toString(),((ErrorAnalyzeResponse) analyzeResult).getContent());
        } else {
            task.updateSiteState(Status.INDEXED.toString());
        }
        return pageService.findPageWithSite(pageUri);
    }


    private Site checkSiteExist(String searchedUrl) {
        String siteUrl = "";
        String siteName = "";
        for (Map.Entry<String, String> entry : namesAndSites.entrySet()) {
            String url = entry.getValue();
            String name = entry.getKey();
            if (searchedUrl.startsWith(url)) {
                siteUrl = url;
                siteName = name;
                break;
            }
        }
        if (siteUrl.isEmpty()) {
            throw new IllegalPageException();
        }
        return siteService.findSiteByUrl(siteUrl, siteName);
    }


}
