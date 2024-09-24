package searchengine.web.entity;

import lombok.Getter;
import lombok.ToString;
import searchengine.services.dto.page.ShowPageDto;

import java.util.List;

@Getter
@ToString
public class SearchResponse {

    private final boolean result;
    private final long count;
    private final List<ShowPageDto> data;

    public SearchResponse(boolean result, long count, List<ShowPageDto> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }
}
