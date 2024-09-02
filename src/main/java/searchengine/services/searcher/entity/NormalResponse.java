package searchengine.services.searcher.entity;

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@ToString
@Getter
public class NormalResponse extends HttpResponseEntity {

    private final Set<String> urls;

    public NormalResponse(int statusCode, String url, String content, Set<String> urls) {
        super(statusCode, url, content);
        this.urls = urls;
    }
}
