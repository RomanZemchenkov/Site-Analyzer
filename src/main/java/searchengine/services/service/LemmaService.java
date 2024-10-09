package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.lemma.LemmaRepository;
import searchengine.services.searcher.lemma.LemmaCreatorContext;
import searchengine.services.searcher.lemma.LemmaCreatorTask;
import searchengine.services.searcher.lemma.LemmaCreatorTaskFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LemmaService {

    private final LemmaRepository lemmaRepository;
    private final LemmaCreatorTaskFactory factory;

    
    public List<Lemma> findAllBySite(Site site){
        return lemmaRepository.findAllBySiteId(site.getId());
    }

    @Transactional
    public List<Lemma> saveBatchLemmas(Map<Lemma, Integer> lemmasAndCounts,List<Lemma> alreadyExistLemmas) {
        List<Lemma> lemmasForSave = new ArrayList<>();
        List<Lemma> lemmasByPage = new ArrayList<>();
        for (Lemma lemma : lemmasAndCounts.keySet()) {
            boolean flag = false;
            for (Lemma mayBeExistLemma : alreadyExistLemmas) {
                if (mayBeExistLemma.equals(lemma)) {
                    lemma = mayBeExistLemma;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                lemmasForSave.add(lemma);
            }
            lemmasByPage.add(lemma);
        }
        alreadyExistLemmas.addAll(lemmaRepository.batchSave(lemmasForSave));
        return lemmasByPage;
    }

    public List<Map<Page, Map<Lemma, Integer>>> lemmasCreator(List<Site> sites){
        return sites.stream()
                .map(site -> lemmaCreator(site,site.getPages()))
                .toList();
    }

    public Map<Page, Map<Lemma, Integer>> lemmaCreator(Site site,List<Page> pages){
        return lemmaCreate(site,pages);
    }

    private Map<Page, Map<Lemma, Integer>> lemmaCreate(Site site, List<Page> pages) {
        LemmaCreatorTask task = taskCreate(site, pages);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Map<Page, Map<Lemma, Integer>> taskResult = forkJoinPool.invoke(task);

        forkJoinPool.shutdown();

        try {
            if (!forkJoinPool.awaitTermination(100L, TimeUnit.MINUTES)) {
                System.err.println("Потоки не завершились за отведенное время");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ожидание завершения потоков было прервано", e);
        }

        LemmaCreatorContext context = task.getContext();
        ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();
        countOfLemmas.forEach(Lemma::setFrequency);

        System.out.println("Количество лемм: " + countOfLemmas.size());
        return taskResult;
    }
    private LemmaCreatorTask taskCreate(Site site, List<Page> pages) {
        LemmaCreatorContext lemmaCreatorContext = new LemmaCreatorContext(site,
                pages, factory, new ConcurrentHashMap<>());
        return factory.createTask(lemmaCreatorContext, new ConcurrentHashMap<>());
    }

}
