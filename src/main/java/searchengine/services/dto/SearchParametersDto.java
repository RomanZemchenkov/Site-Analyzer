package searchengine.services.dto;

import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class SearchParametersDto {

    private final String query;
    private final String limit;
    private final String offset;
    private final String url;

    public SearchParametersDto(String query, String limit, String offset, String url) {
        this.query = query;
        this.limit = limit;
        this.offset = offset;
        this.url = url;
    }
}
