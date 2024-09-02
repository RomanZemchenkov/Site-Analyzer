package searchengine.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.services.dto.SiteProperties;
import searchengine.services.dto.site.CreateSiteDto;
import searchengine.services.searcher.ParseContext;
import searchengine.services.searcher.SiteAnalyzerTask;
import searchengine.services.searcher.SiteAnalyzerTaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private final SiteService siteService;
    private final SiteAnalyzerTaskFactory factory;
    private final SiteProperties properties;
    private Map<String, String> sitesAndNames;

    @PostConstruct
    private void initProperties(){
        List<SiteProperties.Site> sites = properties.getSites();
        sitesAndNames = sites.stream()
                .collect(Collectors.toMap(SiteProperties.Site::getName,SiteProperties.Site::getUrl));
    }

    public void startIndexing() {
        List<ParseContext> contexts = createContext();

        List<SiteAnalyzerTask> firstTaskList = new ArrayList<>();
        for (ParseContext context : contexts) {
            String startUrl = context.getMainUrl();
            firstTaskList.add(factory.createTask(startUrl, context));
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (SiteAnalyzerTask task : firstTaskList) {
            int countOfProcessors = Runtime.getRuntime().availableProcessors();
            int processorsForOneTask = countOfProcessors / firstTaskList.size();
            threadPool.submit(() -> {
                ForkJoinPool forkJoinPool = new ForkJoinPool(processorsForOneTask);
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

    private List<ParseContext> createContext(){
        List<ParseContext> contexts = new ArrayList<>();

        for (Map.Entry<String, String> entry : sitesAndNames.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();

            CreateSiteDto dto = new CreateSiteDto(url, name);
            String siteId = String.valueOf(siteService.createSite(dto));

            ParseContext context = new ParseContext(siteId, name, url, factory);
            contexts.add(context);
        }
        return contexts;
    }
}
