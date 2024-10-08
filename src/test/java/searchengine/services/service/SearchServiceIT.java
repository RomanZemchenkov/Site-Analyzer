package searchengine.services.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.IndexingService;
import searchengine.services.dto.SearchParametersDto;
import searchengine.web.entity.SearchResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class  SearchServiceIT extends BaseTest {

    private final SearchService searchService;
    private final IndexingService indexingAndLemmaService;
    private final static String TEST_TEXT = "Написание скриптов";
    private final static String SITE_URL = "https://itdeti.ru";
    private static final String BASE_LIMIT = "20";
    private static final String BASE_OFFSET = "0";

    @Autowired
    public SearchServiceIT(SearchService searchService, IndexingService indexingAndLemmaService) {
        this.searchService = searchService;
        this.indexingAndLemmaService = indexingAndLemmaService;
    }

    @Test
    @DisplayName("Testing the search for one site without limit and offsets and search was successful")
    void searchForOneSiteTest() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto(TEST_TEXT, BASE_LIMIT, BASE_OFFSET, SITE_URL);


        SearchResponse pages = assertDoesNotThrow(() -> searchService.search(searchParametersDto));

        assertThat(pages.getData()).hasSize(3);
    }

    @Test
    @DisplayName("Testing the search for one site without limit and offsets and search was unsuccessful")
    void unsuccessfulSearchForOneSiteTest() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto("Эти слова не будут найдены", BASE_LIMIT, BASE_OFFSET, SITE_URL);

        SearchResponse showPageDtos = assertDoesNotThrow(() -> searchService.search(searchParametersDto));

        assertThat(showPageDtos.getData()).hasSize(0);
    }

    @Test
    @DisplayName("Testing the search for several sites without limit and offsets and search was successful")
    void successfulSearchForSeveralSites() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto(TEST_TEXT, BASE_LIMIT, BASE_OFFSET, "");

        SearchResponse showPageDtos = assertDoesNotThrow(() -> searchService.search(searchParametersDto));


        assertThat(showPageDtos.getData()).hasSize(4);

    }


}
