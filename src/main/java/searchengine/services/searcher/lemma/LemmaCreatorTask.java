package searchengine.services.searcher.lemma;

import lombok.Getter;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.*;
import java.util.concurrent.*;


@Getter
public class LemmaCreatorTask extends RecursiveTask<List<Lemma>> {

    private final LemmaCreatorContext context;

    public LemmaCreatorTask(LemmaCreatorContext context) {
        this.context = context;
    }

    @Override
    protected List<Lemma> compute() {
        Site site = context.getSite();
        ConcurrentLinkedDeque<Page> pageList = context.getPageList();

        createTask(pageList, site);

        List<Lemma> lemmaList = new ArrayList<>();

        ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();

        countOfLemmas.forEach((Lemma, Integer) -> {
            Lemma.setFrequency(Integer);
            lemmaList.add(Lemma);
        });
        return lemmaList;
    }

    private void createTask(ConcurrentLinkedDeque<Page> pages, Site site) {
        int pageSize = pages.size();
        if (pageSize <= 2) {
            for (Page page : pages) {
                ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();
                LemmaWriter lemmaWriter = new LemmaWriter(countOfLemmas);
                lemmaWriter.createLemma(page, site);
            }
            return;
        }
        ConcurrentLinkedDeque<Page> leftPages = new ConcurrentLinkedDeque<>();
        ConcurrentLinkedDeque<Page> rightPages = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < pageSize; i++) {
            if (i < (pageSize / 2)) {
                Page x = pages.pollFirst();
                leftPages.add(x);
            } else {
                Page e = pages.pollFirst();
                rightPages.add(e);
            }
        }

        LemmaCreatorTaskFactory creatorTaskFactory = context.getCreatorTaskFactory();

        ConcurrentHashMap<Lemma, Integer> countOfLemmas = context.getCountOfLemmas();

        LemmaCreatorContext leftContext = new LemmaCreatorContext(site, leftPages, creatorTaskFactory, countOfLemmas);
        LemmaCreatorContext rightContext = new LemmaCreatorContext(site, rightPages, creatorTaskFactory, countOfLemmas);

        LemmaCreatorTask leftTask = creatorTaskFactory.createTask(leftContext);
        LemmaCreatorTask rightTask = creatorTaskFactory.createTask(rightContext);

        leftTask.fork();
        rightTask.fork();

        leftTask.join();
        rightTask.join();

    }

}
