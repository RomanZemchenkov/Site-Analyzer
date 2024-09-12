package searchengine.services.searcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.PageRepository;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.services.IndexService;
import searchengine.services.IndexingService;
import searchengine.services.LemmaService;
import searchengine.services.dto.page.CreatedPageInfoDto;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;
import searchengine.services.searcher.lemma.LemmaWriter;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingAndLemmaService {

    private final IndexingService indexingService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaCreatorTaskFactory factory;
    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final IndexService indexService;


    public void startIndexingAndCreateLemma() {
        indexingService.startIndexing();
        System.out.println("Индексация и запись окончена");
        List<Site> all = getAllSites();

        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<LemmaCreatorTask> taskList = new ArrayList<>();
        for (Site site : all) {
            LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                    new ConcurrentLinkedDeque<>(site.getPages()), factory, new ConcurrentHashMap<>());
            LemmaCreatorTask task = factory.createTask(lemmaCreatorContext);
            taskList.add(task);
        }


        List<List<Lemma>> results = new ArrayList<>();
        for (LemmaCreatorTask task : taskList) {
            threadPool.submit(() -> {
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                List<Lemma> lemmasList = forkJoinPool.invoke(task);
                results.add(lemmasList);
                System.out.println("Количество лемм для сайта: " + lemmasList.size());

                forkJoinPool.shutdown();
            });
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(100L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (List<Lemma> lemmaList : results) {
            lemmaService.createBatch(lemmaList);
        }


        GlobalVariables.pageAndLemmasWithCount.forEach((Page p, HashMap<Lemma, Integer> map) -> {
            for (Map.Entry<Lemma, Integer> entry : map.entrySet()) {
                Lemma lemma = entry.getKey();
                if (lemma.getId() == null) {
                    System.out.println(lemma);
                    lemmaRepository.save(lemma);
                }
            }
        });


        indexService.createIndex();
        GlobalVariables.pageAndLemmasWithCount.clear();
    }

    public void startIndexingAndCreateLemmaForOnePage(FindPageDto dto){
        CreatedPageInfoDto infoDto = indexingService.onePageIndexing(dto);//
        Site site = infoDto.getSite();
        Page savedPage = infoDto.getSavedPage();
        LemmaWriter lemmaWriter = new LemmaWriter(new ConcurrentHashMap<>());
        lemmaWriter.createLemma(savedPage, site);

        ConcurrentHashMap<Lemma, Integer> lemmasAndCounts = lemmaWriter.getLemmasAndCounts();
        Enumeration<Lemma> keys = lemmasAndCounts.keys();
        Iterator<Lemma> iterator = keys.asIterator();
        Integer siteId = site.getId();
        List<Lemma> allLemmasBySite = lemmaRepository.findAllBySiteId(siteId);
        while (iterator.hasNext()){
            Lemma lemma = iterator.next();
            for(Lemma lem : allLemmasBySite){
                if(lem.equals(lemma)){
                    lem.setFrequency(lem.getFrequency() + 1);
                }
            }
            lemmaRepository.save(lemma);
        }

        indexService.createIndex();
    }

    @Transactional(readOnly = true)
    public List<Site> getAllSites(){
        Set<String> siteNames = indexingService.getNamesAndSites().keySet();
        List<Site> allSites = siteRepository.findAllByName(siteNames);
                /*
        Не понимаю, почему если вызывать этот метод напрямую из тестового класса, то сайты загрузятся вместе со страницами
        Если же это делать через ApiControllerTest - страниц у сайтов не будет
         */
        for (Site site : allSites){
            site.setPages(pageRepository.findAllBySite(site));
        }
        return allSites;
    }

}
