package searchengine.services.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.IndexingAndLemmaService;
import searchengine.services.dto.SearchParametersDto;
import searchengine.services.dto.page.ShowPageDto;

import java.util.*;


public class SearchServiceIT extends BaseTest {

    private final SearchService searchService;
    private final IndexingAndLemmaService indexingAndLemmaService;
    private final static String TEST_TEXT = "Написание скриптов";

    @Autowired
    public SearchServiceIT(SearchService searchService, IndexingAndLemmaService indexingAndLemmaService) {
        this.searchService = searchService;
        this.indexingAndLemmaService = indexingAndLemmaService;
    }

    @Test
    @DisplayName("Testing the search for one site")
    void searchForOneSiteTest() {
        indexingAndLemmaService.startIndexingAndCreateLemma();

        SearchParametersDto searchParametersDto = new SearchParametersDto(TEST_TEXT, "", "", "");


        List<ShowPageDto> pages = searchService.search(searchParametersDto);

        pages.forEach(System.out::println);
    }


}
