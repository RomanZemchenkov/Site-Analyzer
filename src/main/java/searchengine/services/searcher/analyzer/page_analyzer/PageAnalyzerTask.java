package searchengine.services.searcher.analyzer.page_analyzer;

public interface PageAnalyzerTask {

    AnalyzeResponse analyze();

    void updateSiteState(String status);

    void updateSiteState(String status, String content);

    void changeIfStopFlag(boolean flag);
}
