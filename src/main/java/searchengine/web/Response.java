package searchengine.web;

import lombok.Getter;

@Getter
public class Response {


    private String result;
    private String message;

    public Response(String result) {
        this.result = result;
    }

    public Response(String result, String message) {
        this.result = result;
        this.message = message;
    }
}
