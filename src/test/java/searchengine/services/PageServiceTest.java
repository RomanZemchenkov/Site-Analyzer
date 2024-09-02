package searchengine.services;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.dao.model.Site;
import searchengine.dao.repository.SiteRepository;
import searchengine.services.dto.page.CreatePageDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PageServiceTest extends BaseTest {

    private final PageService service;
    private final SiteRepository repository;
    private static final String EXIST_SITE_NAME = "Test Site";
    private static final String EXIST_SITE_ID = "1";
    private static final String EXIST_URL = "/test/url";
    private static final String EXIST_PAGE_PATH = "/path";

    @Autowired
    public PageServiceTest(PageService service, SiteRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Test
    @DisplayName("Тест сохранения страницы")
    void pageSave(){
        CreatePageDto dto = new CreatePageDto(EXIST_SITE_ID, "/path/to", "200", "content");
        String path = assertDoesNotThrow(() -> service.createPage(dto));

        assertThat(path).isEqualTo("/path/to");
    }
}
