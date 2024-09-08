package searchengine.services.searcher.lemma;

import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;
import searchengine.dao.repository.RedisRepository;
import searchengine.services.parser.TextToLemmaParser;

import java.util.*;
import java.util.concurrent.*;


public class LemmaCreatorTask extends RecursiveTask<List<Lemma>> {

    private final RedisRepository redis;
    private final static ConcurrentHashMap<Lemma, Integer> countsOfLemma = new ConcurrentHashMap<>();
    private final LemmaCreatorContext context;
    private final LemmaService lemmaService;

    public LemmaCreatorTask(RedisRepository redis, LemmaCreatorContext context, LemmaService lemmaService) {
        this.redis = redis;
        this.context = context;
        this.lemmaService = lemmaService;
    }

    @Override
    protected List<Lemma> compute() {
        Site site = context.getSite();
        ConcurrentLinkedDeque<Page> pageList = context.getPageList();

        createTask(pageList, site);

//        return countsOfLemma.keys();
        List<Lemma> lemmaList = new ArrayList<>();
        countsOfLemma.forEach((Lemma, Integer) -> {
            Lemma.setFrequency(Integer);
            lemmaList.add(Lemma);
        });
        return lemmaList;
    }

    private void createTask(ConcurrentLinkedDeque<Page> pages, Site site) {
        int pageSize = pages.size();
        if (pageSize <= 2) {
            Iterator<Page> iterator = pages.iterator();
            while (iterator.hasNext()) {
                Page next = iterator.next();
                createLemma(next, site);
            }
            return;
        }
        ConcurrentLinkedDeque<Page> leftPages = new ConcurrentLinkedDeque<>();
        ConcurrentLinkedDeque<Page> rightPages = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < pageSize; i++) {
            if (i <= (pageSize / 2)) {
                Page x = pages.pollFirst();
                leftPages.add(x);
            } else {
                Page e = pages.pollFirst();
                rightPages.add(e);
            }
        }

        LemmaCreatorTaskFactory creatorTaskFactory = context.getCreatorTaskFactory();

        LemmaCreatorContext leftContext = new LemmaCreatorContext(site, leftPages, creatorTaskFactory);
        LemmaCreatorContext rightContext = new LemmaCreatorContext(site, rightPages, creatorTaskFactory);

        LemmaCreatorTask leftTask = creatorTaskFactory.createTask(leftContext);
        LemmaCreatorTask rightTask = creatorTaskFactory.createTask(rightContext);

        leftTask.fork();
        rightTask.fork();

        leftTask.join();
        rightTask.join();

    }

    public void createLemma(Page page, Site site) {
        TextToLemmaParser parser = new TextToLemmaParser();
        String htmlContent = page.getContent();
        HashMap<String, Integer> mapOfLemmas = parser.parse(htmlContent);

        addLemmas(mapOfLemmas, site);
        System.out.println("Лемма создана");
    }

    private void addLemmas(HashMap<String, Integer> mapOfLemmas, Site site) {
        String siteName = site.getName();
        for (Map.Entry<String, Integer> entry : mapOfLemmas.entrySet()) {
            String lemma = entry.getKey();
            Lemma saveLemma = new Lemma(lemma, site);
            countsOfLemma.put(saveLemma, countsOfLemma.getOrDefault(saveLemma, 0) + 1);
        }
    }

//    public void createLemma(Page page, Site site){
//        TextToLemmaParser parser = new TextToLemmaParser();
//        String htmlContent = page.getContent();
//        HashMap<String, Integer> mapOfLemmas = parser.parse(htmlContent);
//
//        checkLemmas(mapOfLemmas,site);
//        System.out.println("Лемма создана");
//    }
//
//    private void checkLemmas(HashMap<String,Integer> mapOfLemmas, Site site){
//        String siteName = site.getName();
//        for(Map.Entry<String,Integer> entry : mapOfLemmas.entrySet()){
//            String lemma = entry.getKey();
//            Lemma saveLemma = new Lemma(lemma, site);
//            redis.saveUseLemma(siteName,saveLemma);
//        }
//    }

//    public List<Lemma> getAllLemmasOnSite(Site site){
//        return redis.getAllLemmasOnSite(site.getName());
//    }

}
