package searchengine.services.searcher.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.services.searcher.entity.ErrorResponse;
import searchengine.services.searcher.entity.HttpResponseEntity;
import searchengine.services.searcher.entity.NormalResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PageAnalyzerTest {

    private static final String mainUrl = "https://sendel.ru";

    @Test
    @DisplayName("Testing that the work searchLink method was successful")
    void successfulSearchLinkTest(){
        PageAnalyzer pageAnalyzer = new PageAnalyzer(mainUrl);
        HttpResponseEntity response = assertDoesNotThrow(() -> pageAnalyzer.searchLink(mainUrl));

        assertThat(response.getClass()).isEqualTo(NormalResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getUrl()).isEqualTo(mainUrl);
        assertThat(response.getContent()).isNotBlank();
    }

    @Test
    @DisplayName("Testing that the work searchLink method was unsuccessful")
    void unsuccessfulSearchLinkTest(){
        PageAnalyzer pageAnalyzer = new PageAnalyzer(mainUrl);
        HttpResponseEntity response = assertDoesNotThrow(() -> pageAnalyzer.searchLink(mainUrl + "/123124124"));

        assertThat(response.getClass()).isEqualTo(ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(404);
    }
}
