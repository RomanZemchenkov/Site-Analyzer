package searchengine.web.entity;

import lombok.Getter;

@Getter
public class ErrorResponse extends Response{

    private final String message;

    public ErrorResponse(String result, String message) {
        super(result);
        this.message = message;
    }
}
