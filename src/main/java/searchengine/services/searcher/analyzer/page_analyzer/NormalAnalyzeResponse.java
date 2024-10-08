package searchengine.services.searcher.analyzer.page_analyzer;

import lombok.Getter;
import searchengine.dao.model.Page;

import java.util.Set;

@Getter
public class NormalAnalyzeResponse extends AnalyzeResponse{

    private final Page page;
    private final Set<String> urls;

    public NormalAnalyzeResponse(Page page, Set<String> urls) {
        this.page = page;
        this.urls = urls;
    }
}
