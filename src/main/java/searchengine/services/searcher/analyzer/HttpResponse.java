package searchengine.services.searcher.analyzer;

import lombok.Getter;
import lombok.ToString;
import org.jsoup.nodes.Document;

@ToString
@Getter
public class HttpResponse {

    private final Integer statusCode;
    private final Document document;

    public HttpResponse(Integer statusCode, Document document) {
        this.statusCode = statusCode;
        this.document = document;
    }
}
