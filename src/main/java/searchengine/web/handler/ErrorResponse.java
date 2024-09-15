package searchengine.web.handler;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String result;
    private final String message;

    public ErrorResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }
}
