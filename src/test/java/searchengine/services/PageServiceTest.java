package searchengine.services;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.BaseTest;
import searchengine.services.dto.page.CreatePageDto;
import searchengine.services.dto.page.CreatePageWithMainSiteUrlDto;
import searchengine.services.dto.page.CreatedPageInfoDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PageServiceTest extends BaseTest {

    private final PageService service;
    private static final String EXIST_SITE_NAME = "Test Site";
    private static final String EXIST_SITE_ID = "1";
    private static final String EXIST_URL = "/test/url";
    private static final String EXIST_PAGE_PATH = "/path";

    @Autowired
    public PageServiceTest(PageService service) {
        this.service = service;
    }

    @Test
    @DisplayName("Тест сохранения страницы с id")
    void pageWithIdSave(){
        CreatePageDto dto = new CreatePageDto(EXIST_SITE_ID, "/path/to", "200", "content");
        String path = assertDoesNotThrow(() -> service.createPage(dto));

        assertThat(path).isEqualTo("/path/to");
    }

    @Test
    @DisplayName("Тест сохранения старницы с mainUrl")
    void pageWithMainUrl(){
        CreatePageWithMainSiteUrlDto dto = new CreatePageWithMainSiteUrlDto(EXIST_URL,"Test site" ,"/path/to/path", "200", "content");
        CreatedPageInfoDto infoDto = assertDoesNotThrow(() -> service.createPage(dto));
        String path = infoDto.getSavedPage().getPath();

        assertThat(path).isEqualTo("/path/to/path");
    }
}
