package searchengine.services.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.searcher.IndexingAndLemmaService;


public class IndexingAndLemmaWriterIT extends BaseTest{

    private final IndexingAndLemmaService service;

    @Autowired
    public IndexingAndLemmaWriterIT(IndexingAndLemmaService service) {
        this.service = service;
    }


    @Test
    @DisplayName("Тестирование работы для одного сайта")
    void startIndexingAndCreateLemmaForOneSite(){
        service.startIndexingAndCreateLemma();
        System.out.println(" ");
    }
}
