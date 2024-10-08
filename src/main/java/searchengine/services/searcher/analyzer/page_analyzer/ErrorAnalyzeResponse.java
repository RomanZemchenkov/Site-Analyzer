package searchengine.services.searcher.analyzer.page_analyzer;

import lombok.Getter;

@Getter
public class ErrorAnalyzeResponse extends AnalyzeResponse{

    private final String content;

    public ErrorAnalyzeResponse(String content) {
        this.content = content;
    }
}
