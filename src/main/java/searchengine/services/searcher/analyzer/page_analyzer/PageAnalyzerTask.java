package searchengine.services.searcher.analyzer.page_analyzer;

import searchengine.services.searcher.entity.HttpResponseEntity;

import java.util.concurrent.ForkJoinPool;

public interface PageAnalyzerTask {

    HttpResponseEntity analyze();

    void updateSiteState(String status);

    void updateSiteState(String status, String content);

    void stopAnalyze(ForkJoinPool forkJoinPool);

    void changeIfStopFlag(boolean flag);
}
