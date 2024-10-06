package searchengine.web.entity;

import lombok.Getter;

@Getter
public abstract class Response {

    private final String result;

    public Response(String result) {
        this.result = result;
    }
}
