package searchengine.web.handler;

import lombok.Getter;

@Getter
public abstract class Response {

    private final String result;

    public Response(String result) {
        this.result = result;
    }
}
