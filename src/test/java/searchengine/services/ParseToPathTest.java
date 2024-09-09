package searchengine.services;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.services.searcher.indexing.PageAnalyzer;
import searchengine.services.searcher.entity.HttpResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ParseToPathTest {

    @Test
    @DisplayName("Тестирование получения первого document")
    void getDocument(){
        PageAnalyzer parse = new PageAnalyzer("https://sendel.ru/");
        HttpResponseEntity normalResponse = assertDoesNotThrow(() -> parse.searchLink("https://sendel.ru/"));
        assertThat(normalResponse.getStatusCode()).isEqualTo(200);
    }
}
