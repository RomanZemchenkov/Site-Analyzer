package searchengine.services.searcher.entity;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ErrorResponse extends HttpResponseEntity{

    public ErrorResponse(int statusCode, String url, String content) {
        super(statusCode, url, content);
    }
}
