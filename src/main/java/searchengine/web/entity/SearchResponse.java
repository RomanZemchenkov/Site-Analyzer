package searchengine.web.entity;

import lombok.Getter;
import lombok.ToString;
import searchengine.services.dto.page.ShowPageDto;

import java.util.List;

@Getter
@ToString
public class SearchResponse extends Response{

    private final long count;
    private final List<ShowPageDto> data;

    public SearchResponse(String result,long count, List<ShowPageDto> data) {
        super(result);
        this.count = count;
        this.data = data;
    }
}
