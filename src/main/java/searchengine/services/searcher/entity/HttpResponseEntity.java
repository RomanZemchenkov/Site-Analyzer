package searchengine.services.searcher.entity;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public abstract class HttpResponseEntity {

    private final int statusCode;
    private final String url;
    private final String content;

    public HttpResponseEntity(int statusCode, String url, String content) {
        this.statusCode = statusCode;
        this.url = url;
        this.content = content;
    }
}
