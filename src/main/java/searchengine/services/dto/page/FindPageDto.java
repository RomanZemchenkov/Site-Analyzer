package searchengine.services.dto.page;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FindPageDto {

    private final String url;

    @JsonCreator
    public FindPageDto(@JsonProperty(value = "url") String url) {
        this.url = url;
    }
}
