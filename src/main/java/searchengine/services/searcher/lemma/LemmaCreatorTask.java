package searchengine.services.searcher.lemma;

import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;


@Getter
public class LemmaCreatorTask extends RecursiveTask<Map<Page, Map<Lemma,Integer>>> {

    private final LemmaCreatorContext context;
    private final ConcurrentHashMap<Page,Map<Lemma,Integer>> countOfLemmasByPages;

    public LemmaCreatorTask(LemmaCreatorContext context, ConcurrentHashMap<Page, Map<Lemma, Integer>> countOfLemmasByPages) {
        this.context = context;
        this.countOfLemmasByPages = countOfLemmasByPages;
    }

    @Override
    protected Map<Page,Map<Lemma,Integer>> compute() {
        Site site = context.getSite();
        List<Page> pageList = context.getPageList();

        createTask(pageList, site);

        return countOfLemmasByPages.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void createTask(List<Page> pages, Site site) {
        int pageSize = pages.size();
        if (pageSize <= 2) {
            for (Page page : pages) {
                ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();
                LemmaWriter lemmaWriter = new LemmaWriter(countOfLemmas);
                CreateLemmaResult result = lemmaWriter.createLemma(page, site);
                countOfLemmasByPages.put(result.getPage(),result.getCountOfLemmasByPage());
            }
            return;
        }
        List<Page> leftPages = new ArrayList<>();
        List<Page> rightPages = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            if (i < (pageSize / 2)) {
                Page x = pages.get(i);
                leftPages.add(x);
            } else {
                Page e = pages.get(i);
                rightPages.add(e);
            }
        }

        LemmaCreatorTaskFactory creatorTaskFactory = context.getCreatorTaskFactory();

        ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();

        LemmaCreatorContext leftContext = new LemmaCreatorContext(site, leftPages, creatorTaskFactory, countOfLemmas);
        LemmaCreatorContext rightContext = new LemmaCreatorContext(site, rightPages, creatorTaskFactory, countOfLemmas);

        LemmaCreatorTask leftTask = creatorTaskFactory.createTask(leftContext, countOfLemmasByPages);
        LemmaCreatorTask rightTask = creatorTaskFactory.createTask(rightContext, countOfLemmasByPages);

        leftTask.fork();
        rightTask.fork();

        leftTask.join();
        rightTask.join();

    }

}
