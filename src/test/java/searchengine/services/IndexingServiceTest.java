package searchengine.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;

public class IndexingServiceTest extends BaseTest {

    private final IndexingService service;

    @Autowired
    public IndexingServiceTest(IndexingService service) {
        this.service = service;
    }

    @Test
    void test(){
        Assertions.assertDoesNotThrow(service::startIndexing);

        System.out.println(" ");
    }
}
