package searchengine.services.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.IndexingAndLemmaService;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.dto.page.ShowPageDto;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class  SearchServiceIT extends BaseTest {

    private final SearchService searchService;
    private final IndexingAndLemmaService indexingAndLemmaService;
    private final static String TEST_TEXT = "Написание скриптов";
    private final static String SITE_URL = "https://itdeti.ru";
    private static final String BASE_LIMIT = "20";
    private static final String BASE_OFFSET = "0";

    @Autowired
    public SearchServiceIT(SearchService searchService, IndexingAndLemmaService indexingAndLemmaService) {
        this.searchService = searchService;
        this.indexingAndLemmaService = indexingAndLemmaService;
    }

    @Test
    @DisplayName("Testing the search for one site without limit and offsets and search was successful")
    void searchForOneSiteTest() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto(TEST_TEXT, BASE_LIMIT, BASE_OFFSET, SITE_URL);


        HashMap<Integer, List<ShowPageDto>> pages = assertDoesNotThrow(() -> searchService.search(searchParametersDto));

        assertThat(pages).hasSize(1);
        assertThat(pages.get(0)).hasSize(3);
    }

    @Test
    @DisplayName("Testing the search for one site without limit and offsets and search was unsuccessful")
    void unsuccessfulSearchForOneSiteTest() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto("Эти слова не будут найдены", BASE_LIMIT, BASE_OFFSET, SITE_URL);

        HashMap<Integer, List<ShowPageDto>> showPageDtos = assertDoesNotThrow(() -> searchService.search(searchParametersDto));

        assertThat(showPageDtos).hasSize(0);
    }

    @Test
    @DisplayName("Testing the search for several sites without limit and offsets and search was successful")
    void successfulSearchForSeveralSites() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto(TEST_TEXT, BASE_LIMIT, BASE_OFFSET, "");

        HashMap<Integer, List<ShowPageDto>> showPageDtos = assertDoesNotThrow(() -> searchService.search(searchParametersDto));


        assertThat(showPageDtos).hasSize(1);
        assertThat(showPageDtos.get(0)).hasSize(4);

    }


}
