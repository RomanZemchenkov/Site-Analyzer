package searchengine.services.searcher.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.services.searcher.analyzer.page_analyzer.PageAnalyzerImpl;
import searchengine.services.searcher.entity.ErrorResponse;
import searchengine.services.searcher.entity.HttpResponseEntity;
import searchengine.services.searcher.entity.NormalResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PageAnalyzerImplTest {

    private static final String mainUrl = "https://shcool11-balakhna.edusite.ru";

    @Test
    @DisplayName("Testing that the work searchLink method was successful")
    void successfulSearchLinkTest(){
        PageAnalyzerImpl pageAnalyzerImpl = new PageAnalyzerImpl();
        HttpResponseEntity response = assertDoesNotThrow(() -> pageAnalyzerImpl.searchLink(mainUrl, mainUrl));

        assertThat(response.getClass()).isEqualTo(NormalResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getUrl()).isEqualTo(mainUrl);
        assertThat(response.getContent()).isNotBlank();
    }

    @Test
    @DisplayName("Testing that the work searchLink method was unsuccessful")
    void unsuccessfulSearchLinkTest(){
        PageAnalyzerImpl pageAnalyzerImpl = new PageAnalyzerImpl();
        HttpResponseEntity response = assertDoesNotThrow(() -> pageAnalyzerImpl.searchLink(mainUrl + "/123124124", mainUrl));

        assertThat(response.getClass()).isEqualTo(ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(404);
    }
}
