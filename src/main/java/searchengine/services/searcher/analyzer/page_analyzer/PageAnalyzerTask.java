package searchengine.services.searcher.analyzer.page_analyzer;

import searchengine.services.searcher.entity.HttpResponseEntity;


public interface PageAnalyzerTask {

    HttpResponseEntity analyze();

    void updateSiteState(String status);

    void updateSiteState(String status, String content);

    void changeIfStopFlag(boolean flag);
}