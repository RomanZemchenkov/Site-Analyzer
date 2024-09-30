package searchengine.services.searcher.analyzer;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.model.Status;
import searchengine.services.dto.site.ShowSiteDto;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static searchengine.services.GlobalVariables.*;

@Service
@RequiredArgsConstructor
public class IndexingImpl implements Indexing{

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

    public void startSitesIndexing() {
        INDEXING_STARTED = true;
        createContext();

        List<SiteAnalyzerTask> firstTasksList = new ArrayList<>();
        for (ParseContext context : contexts) {
            String startUrl = context.getSiteDto().getUrl();
            firstTasksList.add(factory.createTask(startUrl, context, new ConcurrentSkipListSet<>()));
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < firstTasksList.size(); i++) {
            SiteAnalyzerTask task = firstTasksList.get(i);
            ParseContext context = contexts.get(i);
            int countOfParallel = Math.max(1, COUNT_OF_PROCESSORS / firstTasksList.size());
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
        for (ParseContext context : contexts) {
            context.setIfErrorResponse(true);
        }

        Map<SiteAnalyzerTask, ForkJoinPool> localPools = new HashMap<>(pools);
        for (Map.Entry<SiteAnalyzerTask, ForkJoinPool> entry : localPools.entrySet()) {
            SiteAnalyzerTask task = entry.getKey();
            ForkJoinPool pool = entry.getValue();
            task.stopIndexing(pool);
            task.updateSiteState(Status.FAILED.toString(),STOP_INDEXING_TEXT);
        }
    }

    private void executeTask(SiteAnalyzerTask task, ParseContext context, int countOfParallel) {
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
                task.updateSiteState(Status.INDEXED.toString());
            }
        }
    }

    private void createContext() {
        contexts = new ArrayList<>();

        for (Map.Entry<String, String> entry : namesAndSites.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();

            CreateSiteDto dto = new CreateSiteDto(url, name);
            ShowSiteDto site = siteService.createSite(dto);

            ParseContext context = new ParseContext(site, factory);
            contexts.add(context);
        }

    }

    public FindPageDto startPageIndexing(String searchedUrl) {
        ShowSiteDto showSite = checkSiteExist(searchedUrl);
        PageParseContext pageContext = new PageParseContext(showSite);
        PageAnalyzerTask task = pageFactory.createTask(searchedUrl, pageContext);
        task.analyze();

        String pageUri = searchedUrl.substring(showSite.getUrl().length());
        task.updateSiteState(Status.INDEXED.toString());
        return pageService.findPageWithSite(pageUri);
    }


    private ShowSiteDto checkSiteExist(String searchedUrl){
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
