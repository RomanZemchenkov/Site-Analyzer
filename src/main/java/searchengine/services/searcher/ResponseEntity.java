package searchengine.services.searcher;

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@ToString
@Getter
public class ResponseEntity {

    private final Integer statusCode;
    private final String url;
    private final String content;
    private final Set<String> urls;

    public ResponseEntity(Integer statusCode, String url, String content, Set<String> urls) {
        this.statusCode = statusCode;
        this.url = url;
        this.content = content;
        this.urls = urls;
    }
}
