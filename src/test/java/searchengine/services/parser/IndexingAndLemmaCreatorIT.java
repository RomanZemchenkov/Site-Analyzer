package searchengine.services.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import searchengine.BaseTest;
import searchengine.services.dto.page.FindPageDto;
import searchengine.services.searcher.IndexingAndLemmaService;


@Sql(value = "classpath:sql/init.sql")
public class IndexingAndLemmaCreatorIT extends BaseTest{

    private final IndexingAndLemmaService service;

    @Autowired
    public IndexingAndLemmaCreatorIT(IndexingAndLemmaService service) {
        this.service = service;
    }


    @Test
    @DisplayName("Тестирование работы для одного сайта")
    void startIndexingAndCreateLemmaForOneSite(){
        service.startIndexingAndCreateLemma();
    }

    @Test
    @DisplayName("Тестирование сохранения лемм и индексов для одной страницы")
    void startIndexingAndCreateLemmaForOnePage(){
        FindPageDto findPageDto = new FindPageDto("https://sendel.ru/books/");
        service.startIndexingAndCreateLemmaForOnePage(findPageDto);
    }
}
